<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2014 IBM Corp.
  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
  
   http://www.apache.org/licenses/LICENSE-2.0
  
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<!-- 

  This stylesheet rewrites an XML config file (e.g. export.xml) based on the
  information in the rewriteInfo parameter.

-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:dcm="urn:datapower:configuration:manager"
  xmlns:date="http://exslt.org/dates-and-times"
  xmlns:dyn="http://exslt.org/dynamic"
  xmlns:exslt="http://exslt.org/common"
  xmlns:func="http://exslt.org/functions"
  xmlns:local="urn:local:function"
  xmlns:mgmt="http://www.datapower.com/schemas/management"
  xmlns:redir="http://xml.apache.org/xalan/redirect"
  xmlns:redirect="http://xml.apache.org/xalan/redirect"
  xmlns:rx="urn:local-function:rewrite-xmlconfig"
  xmlns:str="http://exslt.org/strings"
  xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
  xmlns:wsdlsoap11="http://schemas.xmlsoap.org/wsdl/soap/"
  xmlns:wsdlsoap12="http://schemas.xmlsoap.org/wsdl/soap12/"
  extension-element-prefixes="date dyn exslt func redir str" 
  exclude-result-prefixes="date dyn exslt func redir str">
  
  
<!--  <xsl:include href="nodesetUtils.xsl"/> -->


  <xsl:param name="param1"/>
  <xsl:variable name="dcmpolicy" select="document($param1)"/>
  
  
  <xsl:template match="/">
    
    <xsl:variable name="afterRewriteNames">
      <xsl:choose>
        <xsl:when test="$dcmpolicy/dcm:definition/dcm:object-name">
          
          <!-- Rewrite the XML configuration to change object names and references. -->
          <xsl:copy-of select="rx:renameDPObjects(., exslt:node-set($dcmpolicy/dcm:definition/dcm:object-name))"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <!-- No rewriting of names needed. -->
          <xsl:copy-of select="."/>
          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="afterReplacingWSDLs">
      <xsl:choose>
        <xsl:when test="$dcmpolicy/dcm:definition/dcm:wsdl">
          
          <!-- Rewrite the XML configuration to replace WSDLs. -->
          <xsl:copy-of select="rx:replaceWSDLs(exslt:node-set($afterRewriteNames), exslt:node-set($dcmpolicy/dcm:definition/dcm:wsdl/wsdl:definitions))"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <!-- No replacing of WSDLs needed. -->
          <xsl:copy-of select="$afterRewriteNames"/>
          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:copy-of select="$afterReplacingWSDLs"/>
    
  </xsl:template>
  
  <!-- 
    Make a pass through the supplied DP export replacing the object names that
    match the dcm:object-name elements.
    
    Object references are also updated, but we may miss a few. Most object references
    are of the form <Xyzzy class="...">...</Xyzzy> so these are easy to catch.  A few,
    however, are simply of the form <Xyzzy>...</Xyzzy>, and these have to be handled
    on a case-by-case basis.  Update the code as we discover object references of this
    second form.
  -->
  <func:function name="rx:renameDPObjects">
    <xsl:param name="dpexport"/>
    <xsl:param name="rename-info"/>
    <func:result>
      <xsl:apply-templates select="$dpexport" mode="rx:renameDPObjects">
        <xsl:with-param name="rename-info" select="$rename-info"/>
      </xsl:apply-templates>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:renameDPObjects">
        <xsl:with-param name="rename-info" select="$rename-info"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="file" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <!-- <file @name> doesn't really need to be subject to renaming. ;) -->
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <!-- Add here when you find elements that are object references without @class. -->
  <xsl:template match="*[@class!=''] | StylePolicyAction/SLMPolicy | StylePolicyAction/Condition/ConditionAction" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="rx:renameDPObjects">
        <xsl:with-param name="rename-info" select="$rename-info"/>
      </xsl:apply-templates>
      
      <!-- This element is an object reference, so munge its text, if appropriate. -->
      <xsl:variable name="actualName" select="string()"/>
      <xsl:variable name="renames" select="$rename-info[starts-with($actualName, @match)]"/>
      <xsl:choose>
        <xsl:when test="$renames">
          <xsl:value-of select="concat($renames[1]/@replace-with, substring(string(), string-length($renames[1]/@match) + 1))"/>
          <!-- <xsl:message> replaced reference <xsl:value-of select="concat(name(..), '/@name=', string())"/> with <xsl:value-of select="concat($renames[1]/@replace-with, substring(string(), string-length($renames[1]/@match) + 1))"/></xsl:message> -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="string()"/>
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:renameDPObjects">
        <xsl:with-param name="rename-info" select="$rename-info"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@name" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <xsl:attribute name="name">
      <!-- <xsl:message> checking @name <xsl:value-of select="concat(name(..), '/@name=', string())"/></xsl:message> -->
      <xsl:variable name="actualName" select="string()"/>
      <xsl:variable name="renames" select="$rename-info[starts-with($actualName, @match)]"/>
      <xsl:choose>
        <xsl:when test="$renames">
          <xsl:value-of select="concat($renames[1]/@replace-with, substring(string(), string-length($renames[1]/@match) + 1))"/>
          <!-- <xsl:message> replaced @name <xsl:value-of select="concat(name(..), '/@name=', string())"/> with <xsl:value-of select="concat($renames[1]/@replace-with, substring(string(), string-length($renames[1]/@match) + 1))"/></xsl:message> -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="string()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:renameDPObjects">
    <xsl:param name="rename-info"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  








  <!-- 
    This function oversees the process of rewriting the XML configuration to replace the
    current set of WSDLs with the new set.  It is a somewhat daunting process to follow.
  -->
  <func:function name="rx:replaceWSDLs">
    <xsl:param name="dpexport"/>
    <xsl:param name="new-wsdls"/>
    <func:result>
      
      <xsl:choose>
        <xsl:when test="count($dpexport/datapower-configuration/configuration/WSGateway) = 0">
          <xsl:message terminate="yes">The service definition specifies one or more WSDLs to place into the web service proxy, but no web service proxy is present in the DP export.</xsl:message>
        </xsl:when>
        <xsl:when test="count($dpexport/datapower-configuration/configuration/WSGateway) = 1">
          
          <!--
            So we have a web service proxy and wsdl(s) and we need to spin through them to
            produce a new WSEndpointRewrite object to fold into the web service proxy
            configuration.
          -->
          <xsl:variable name="proxyname" select="$dpexport/datapower-configuration/configuration/WSGateway/@name"/>
          <xsl:variable name="oldEndpoint" select="$dpexport/datapower-configuration/configuration/WSEndpointRewritePolicy[@name = $proxyname]"/>
          <xsl:variable name="newEndpoint" select="rx:generateNewEndpointRewrite($new-wsdls, exslt:node-set($oldEndpoint))"/>
          
          <!-- Now replace the WSEndpointRewritePolicy[@name=$proxyname] with the new one that we just generated. -->
          <xsl:variable name="rewritePolicy" select="rx:replaceEndpointRewritePolicy($dpexport, $proxyname, $newEndpoint)"/>
          
          <!--
            The web service proxy has a WSGateway/BaseWSDL and a PolicyAttachment for each WSDL.  Create new ones
            representing the new sets of WSDLs and replace the existing ones with the new ones.
          -->
          <xsl:variable name="oldBaseWSDL" select="$dpexport/datapower-configuration/configuration/WSGateway/BaseWSDL[1]"/>
          <xsl:variable name="newBaseWSDLs" select="rx:generateNewBaseWSDLs($new-wsdls, exslt:node-set($oldBaseWSDL))"/>
          <xsl:variable name="oldPolicyAttachment" select="$dpexport/datapower-configuration/configuration/PolicyAttachments[1]"/>
          <xsl:variable name="newPolicyAttachments" select="rx:generateNewPolicyAttachments(exslt:node-set($newBaseWSDLs), exslt:node-set($oldPolicyAttachment))"/>
          <xsl:variable name="rewriteWSDLReferences" select="rx:replaceWSDLReferences(exslt:node-set($rewritePolicy), exslt:node-set($newBaseWSDLs), exslt:node-set($newPolicyAttachments))"/>
          
          <!-- Return the mutated masterpiece. -->
          <xsl:copy-of select="$rewriteWSDLReferences"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <xsl:message terminate="yes">The service definition specifies one or more WSDLs to place into the web service proxy, but more than one web service proxy is present in the DP export, so it isn't clear which WSDLs go with which web service proxies.  One WSP per DP export file, please.</xsl:message>
          
        </xsl:otherwise>
      </xsl:choose>
      
    </func:result>
  </func:function>
  
  
  
  
  
  <!-- 
    This function generates a set of BaseWSDL elements based on the supplied set of wsdl:definitions
    and a BaseWSDL template.
  -->
  <func:function name="rx:generateNewBaseWSDLs">
    <xsl:param name="wsdls"/>
    <xsl:param name="templateBaseWSDL"/>
    <func:result>
      <!-- 
        A BaseWSDL element looks like this (may change with firmware updates):
        
        <BaseWSDL>
          <WSDLSourceLocation>local:///datetime.wsdl</WSDLSourceLocation>
          <WSDLName>datetime.wsdl</WSDLName>
          <PolicyAttachments class="PolicyAttachments">enrolement_datetime.wsdl</PolicyAttachments>
        </BaseWSDL>
        
        The dcm:wsdl parent of each WSDL has two important attributes.  @href is the WSDL to access
        during processing on this computer, while the optional @source is the WSDL to access from DP.
        By default, if @source isn't present, local:///@href is used on DP.
      -->
      <xsl:for-each select="$wsdls">
        
        <xsl:variable name="dcmParent" select="ancestor::dcm:wsdl[1]"/>
        
        <!-- <xsl:message>### generateNewBaseWSDLs : cmanParent=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set($cmanParent), 1)"/></xsl:message> -->

        <xsl:variable name="sourceURIonDP">
          <xsl:choose>
            <xsl:when test="$dcmParent/@source!=''">
              <xsl:value-of select="$dcmParent/@source"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat('local:///', $dcmParent/@href)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
<!--        
        <xsl:message>### generateNewBaseWSDLs : sourceURIonDP=<xsl:value-of select="$sourceURIonDP"/></xsl:message>
        <xsl:message>### generateNewBaseWSDLs : templateBaseWSDL/PolicyAttachments=<xsl:value-of select="$templateBaseWSDL/PolicyAttachments"/></xsl:message>
-->        
        <xsl:variable name="oldWSDLname" select="string($templateBaseWSDL/WSDLName)"/>
        <xsl:variable name="newWSDLName" select="rx:basename($sourceURIonDP)"/>
        <xsl:variable name="rootPolicyAttachmentName" select="substring($templateBaseWSDL/PolicyAttachments, 1, string-length($templateBaseWSDL/PolicyAttachments) - string-length($oldWSDLname))"/>
        <xsl:variable name="newPolicyAttachmentName" select="concat($rootPolicyAttachmentName, $newWSDLName)"/>
<!--        
        <xsl:message>### generateNewBaseWSDLs : oldWSDLname=<xsl:value-of select="$oldWSDLname"/></xsl:message>
        <xsl:message>### generateNewBaseWSDLs : newWSDLName=<xsl:value-of select="$newWSDLName"/></xsl:message>
        <xsl:message>### generateNewBaseWSDLs : rootPolicyAttachmentName=<xsl:value-of select="$rootPolicyAttachmentName"/></xsl:message>
        <xsl:message>### generateNewBaseWSDLs : newPolicyAttachmentName=<xsl:value-of select="$newPolicyAttachmentName"/></xsl:message>
-->        
        <xsl:variable name="newBaseWSDL">
          <xsl:apply-templates select="$templateBaseWSDL" mode="rx:generateNewBaseWSDLs">
            <xsl:with-param name="sourceURIonDP" select="$sourceURIonDP"/>
            <xsl:with-param name="newWSDLName" select="$newWSDLName"/>
            <xsl:with-param name="newPolicyAttachmentName" select="$newPolicyAttachmentName"/>
          </xsl:apply-templates>
        </xsl:variable>
        
        <!-- <xsl:message>### generateNewBaseWSDLs : newBaseWSDL=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set($newBaseWSDL))"/></xsl:message> -->
        
        <xsl:copy-of select="$newBaseWSDL"/>
        
      </xsl:for-each>
      
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:generateNewBaseWSDLs">
        <xsl:with-param name="sourceURIonDP" select="$sourceURIonDP"/>
        <xsl:with-param name="newWSDLName" select="$newWSDLName"/>
        <xsl:with-param name="newPolicyAttachmentName" select="$newPolicyAttachmentName"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="PolicyAttachments" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$newPolicyAttachmentName"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSDLName" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$newWSDLName"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSDLSourceLocation" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$sourceURIonDP"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:generateNewBaseWSDLs">
        <xsl:with-param name="sourceURIonDP" select="$sourceURIonDP"/>
        <xsl:with-param name="newWSDLName" select="$newWSDLName"/>
        <xsl:with-param name="newPolicyAttachmentName" select="$newPolicyAttachmentName"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:generateNewBaseWSDLs">
    <xsl:param name="sourceURIonDP"/>
    <xsl:param name="newWSDLName"/>
    <xsl:param name="newPolicyAttachmentName"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <!-- 
    This function generates a set of PolicyAttachment objects that will satisfy the references in
    the BaseWSDLs to those self-same PolicyAttachment objects.
  -->
  <func:function name="rx:generateNewPolicyAttachments">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="templatePolicyAttachment"/>
    <func:result>
      <!-- 
        Currently a PolicyAttachment object looks like this, though it may change as new
        firmware updates come out:
        
        <PolicyAttachments name="enrolement_datetime.wsdl">
          <mAdminState>enabled</mAdminState>
          <EnforcementMode>enforce</EnforcementMode>
          <PolicyReferences>on</PolicyReferences>
        </PolicyAttachments>
      -->
      <xsl:for-each select="$newBaseWSDLs">
        
        <xsl:variable name="newPolicyAttachments">
          <xsl:apply-templates select="$templatePolicyAttachment" mode="rx:generateNewPolicyAttachments">
            <xsl:with-param name="name" select="string(BaseWSDL/PolicyAttachments)"/>
          </xsl:apply-templates>
        </xsl:variable>
        
        <!-- <xsl:message>### generateNewPolicyAttachments : templatePolicyAttachment=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set($templatePolicyAttachment))"/></xsl:message> -->
        <!-- <xsl:message>### generateNewPolicyAttachments : BaseWSDL=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set(.))"/></xsl:message> -->
        <!-- <xsl:message>### generateNewPolicyAttachments : name=<xsl:value-of select="string(BaseWSDL/PolicyAttachments)"/></xsl:message> -->
        <!-- <xsl:message>### generateNewPolicyAttachments : newPolicyAttachment=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set($newPolicyAttachments))"/></xsl:message> -->
        
        <xsl:copy-of select="$newPolicyAttachments"/>
        
      </xsl:for-each>

    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:generateNewPolicyAttachments">
    <xsl:param name="name"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:generateNewPolicyAttachments">
        <xsl:with-param name="name" select="$name"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="PolicyAttachments/@name" mode="rx:generateNewPolicyAttachments">
    <xsl:param name="name"/>
    <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:generateNewPolicyAttachments">
    <xsl:param name="name"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:generateNewPolicyAttachments">
        <xsl:with-param name="name" select="$name"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:generateNewPolicyAttachments">
    <xsl:param name="name"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <!-- 
    This function rewrites the supplied DP export (/datapower-configuration/...), replacing
    the WSGateway/BaseWSDL and PolicyAttachments objects with the new ones.
  -->
  <func:function name="rx:replaceWSDLReferences">
    <xsl:param name="dpexport"/>
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <func:result>
      <xsl:variable name="result">
        <xsl:apply-templates select="$dpexport" mode="rx:replaceWSDLReferences">
          <xsl:with-param name="newBaseWSDLs" select="$newBaseWSDLs"/>
          <xsl:with-param name="newPolicyAttachments" select="$newPolicyAttachments"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:copy-of select="exslt:node-set($result)"/>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:replaceWSDLReferences">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:replaceWSDLReferences">
        <xsl:with-param name="newBaseWSDLs" select="$newBaseWSDLs"/>
        <xsl:with-param name="newPolicyAttachments" select="$newPolicyAttachments"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSGateway/BaseWSDL" mode="rx:replaceWSDLReferences">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <xsl:if test="name(preceding-sibling::*[1]) != name()">
      <!-- This is the first BaseWSDL element, so stick all the new BaseWSDL elements here. -->
      <xsl:copy-of select="$newBaseWSDLs"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="PolicyAttachments" mode="rx:replaceWSDLReferences">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <xsl:if test="name(preceding-sibling::*[1]) != name()">
      <!-- This is the first PolicyAttachments element, so stick all the new PolicyAttachments elements here. -->
      <xsl:copy-of select="$newPolicyAttachments"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:replaceWSDLReferences">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:replaceWSDLReferences">
        <xsl:with-param name="newBaseWSDLs" select="$newBaseWSDLs"/>
        <xsl:with-param name="newPolicyAttachments" select="$newPolicyAttachments"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:replaceWSDLReferences">
    <xsl:param name="newBaseWSDLs"/>
    <xsl:param name="newPolicyAttachments"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <!-- 
    This function rewrites the supplied DP export (/datapower-configuration/...), replacing
    the WSEndpointRewritePolicy object with the new one.
  -->
  <func:function name="rx:replaceEndpointRewritePolicy">
    <xsl:param name="dpexport"/>
    <xsl:param name="proxyname"/>
    <xsl:param name="newRewritePolicy"/>
    <func:result>
      <xsl:variable name="result">
        <xsl:apply-templates select="$dpexport" mode="rx:replaceEndpointRewritePolicy">
          <xsl:with-param name="proxyname" select="$proxyname"/>
          <xsl:with-param name="newRewritePolicy" select="$newRewritePolicy"/>
        </xsl:apply-templates>
      </xsl:variable>
      <xsl:copy-of select="exslt:node-set($result)"/>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:replaceEndpointRewritePolicy">
    <xsl:param name="proxyname"/>
    <xsl:param name="newRewritePolicy"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:replaceEndpointRewritePolicy">
        <xsl:with-param name="proxyname" select="$proxyname"/>
        <xsl:with-param name="newRewritePolicy" select="$newRewritePolicy"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSEndpointRewritePolicy" mode="rx:replaceEndpointRewritePolicy">
    <xsl:param name="proxyname"/>
    <xsl:param name="newRewritePolicy"/>
    <xsl:choose>
      <xsl:when test="@name = $proxyname">
        <!-- Replace this element with the new rewrite policy. -->
        <xsl:copy-of select="$newRewritePolicy"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- This isn't the rewrite policy that we need to replace, leave it alone. -->
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="rx:replaceEndpointRewritePolicy">
            <xsl:with-param name="proxyname" select="$proxyname"/>
            <xsl:with-param name="newRewritePolicy" select="$newRewritePolicy"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:replaceEndpointRewritePolicy">
    <xsl:param name="proxyname"/>
    <xsl:param name="newRewritePolicy"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:replaceEndpointRewritePolicy">
        <xsl:with-param name="proxyname" select="$proxyname"/>
        <xsl:with-param name="newRewritePolicy" select="$newRewritePolicy"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:replaceEndpointRewritePolicy">
    <xsl:param name="proxyname"/>
    <xsl:param name="newRewritePolicy"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  
  <!--
    Generate a new WSEndpointRewritePolicy based on an existing WSEndpointRewritePolicy
    and one or more WSDLs.  The reason for using the old policy is to get the structure
    of the XML right, which can change from time to time, usually adding new fields for
    which default values are acceptable.
  -->
  <func:function name="rx:generateNewEndpointRewrite">
    <xsl:param name="wsdls"/>
    <xsl:param name="oldEndpoint"/>
    <func:result>
      
      <!-- Capture the current local and remote rewrite rules which we will use as templates for our new rewrite rules. -->
      <xsl:variable name="oldLocalRewrite" select="$oldEndpoint/WSEndpointLocalRewriteRule[1]"/>
      <xsl:variable name="oldRemoteRewrite" select="$oldEndpoint/WSEndpointRemoteRewriteRule[1]"/>
      
      <!-- Generate new local and remote rewrite rules by scanning each WSDL. -->
      <xsl:variable name="newRewriteRules">
        
        <xsl:for-each select="$wsdls">
          <xsl:variable name="local" select="exslt:node-set($oldLocalRewrite)"/>
          <xsl:variable name="remote" select="exslt:node-set($oldRemoteRewrite)"/>
          <xsl:copy-of select="rx:generateNewRewriteRules(., $local, $remote)"/>
        </xsl:for-each>
        
      </xsl:variable>
      
      <xsl:apply-templates select="$oldEndpoint" mode="rx:generateNewEndpointRewrite">
        <xsl:with-param name="newRules" select="exslt:node-set($newRewriteRules)"/>
      </xsl:apply-templates>
      
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:generateNewEndpointRewrite">
    <xsl:param name="newRules"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:generateNewEndpointRewrite">
        <xsl:with-param name="newRules" select="$newRules"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSEndpointLocalRewriteRule" mode="rx:generateNewEndpointRewrite">
    <xsl:param name="newRules"/>
    <xsl:if test="name(preceding-sibling::*[1]) != name()">
      <!-- This is the first local rewrite rule, so stick all the new local rewrite rules here. -->
      <xsl:copy-of select="$newRules/WSEndpointLocalRewriteRule"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="WSEndpointRemoteRewriteRule" mode="rx:generateNewEndpointRewrite">
    <xsl:param name="newRules"/>
    <xsl:if test="name(preceding-sibling::*[1]) != name()">
      <!-- This is the first remote rewrite rule, so stick all the new remote rewrite rules here. -->
      <xsl:copy-of select="$newRules/WSEndpointRemoteRewriteRule"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:generateNewEndpointRewrite">
    <xsl:param name="newRules"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:generateNewEndpointRewrite">
        <xsl:with-param name="newRules" select="$newRules"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:generateNewEndpointRewrite">
    <xsl:param name="newRules"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <!--
    Generate a new set of local and remote rewrite rules based on the supplied WSDL, which
    is the content of a WSDL, not a WSDL URL.  The two template parameters are used as patterns
    when creating the new rewrite rules.
  -->
  <func:function name="rx:generateNewRewriteRules">
    <xsl:param name="wsdl"/>
    <xsl:param name="templateLocalRewrite"/>
    <xsl:param name="templateRemoteRewrite"/>
    <func:result>
      
      <!-- <xsl:message>### generateNewRewriteRules : wsdl=<xsl:value-of select="local:dumpNodesetAsText(exslt:node-set($wsdl/parent::*), 2)"/></xsl:message> -->
      
      <!-- Generate a new pair of rewrite rules for each wsdl:port -->
      <xsl:for-each select="$wsdl/wsdl:service/wsdl:port">
        <!-- Extract the name of the binding associated with this port, which may either have a namespace, or a targetNamespace prefix such as tns: -->
        <xsl:variable name="bindingName">
          <xsl:choose>
            <xsl:when test="contains(@binding, ':')">
              <xsl:value-of select="substring-after(@binding, ':')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@binding"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        
        <!-- The ServicePortMatchRegex is common to both rewrite rules. -->
        <xsl:variable name="regex" select="concat('^{', $wsdl/@targetNamespace, '}', @name, '$')"/>
        <!-- <xsl:message>### regex=<xsl:value-of select="$regex"/></xsl:message> -->
        
        <!-- Determine whether this is soap 1.1 or soap 1.2 -->
        <xsl:variable name="bindingProtocol">
          <!-- <xsl:message>### bingindName=<xsl:value-of select="$bindingName"/></xsl:message> -->
          <xsl:variable name="bindingNS" select="namespace-uri($wsdl/wsdl:binding[@name=$bindingName]/*[local-name()='binding'])"/>
          <!-- <xsl:message>### bindingNS=<xsl:value-of select="$bindingNS"/></xsl:message> -->
          <xsl:choose>
            <xsl:when test="$bindingNS = 'http://schemas.xmlsoap.org/wsdl/soap/'">
              <xsl:value-of select="'soap-11'"/>
            </xsl:when>
            <xsl:when test="$bindingNS = 'http://schemas.xmlsoap.org/wsdl/soap12/'">
              <xsl:value-of select="'soap-12'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'default'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!-- <xsl:message>### bindingProtocol=<xsl:value-of select="$bindingProtocol"/></xsl:message> -->
        
        <!--
          The rewrite rule is centered on the backend URL.  Initially this comes from the WSDL
          and may then be overridden by a dcm:backend-url in the parent deployment definition.
        -->
        <xsl:variable name="remoteURL">
          
          <xsl:variable name="parentDcmWsdl" select="$wsdl/.."/>
          <xsl:variable name="parentRewriteInfo" select="$parentDcmWsdl/.."/>
          <xsl:variable name="override" select="$parentRewriteInfo/dcm:backend-url[@wsdl=$parentDcmWsdl/@href]/@url"/>
<!--          
          <xsl:message>remoteURL name($parentRewriteInfo)=<xsl:value-of select="name($parentRewriteInfo)"/></xsl:message>
          <xsl:message>remoteURL name($parentCmanWsdl)=<xsl:value-of select="name($parentCmanWsdl)"/></xsl:message>
          <xsl:message>remoteURL name($parentRewriteInfo/dcm:backend-url)=<xsl:value-of select="name($parentRewriteInfo/dcm:backend-url)"/></xsl:message>
          <xsl:message>remoteURL name($parentRewriteInfo/dcm:backend-url[@wsdl=$parentCmanWsdl/@href])=<xsl:value-of select="name($parentRewriteInfo/dcm:backend-url[@wsdl=$parentCmanWsdl/@href])"/></xsl:message>
          <xsl:message>remoteURL override=<xsl:value-of select="$override"/></xsl:message>
-->          
          <xsl:choose>
            <xsl:when test="$override">
              <!-- Use the override specified in <dcm:backend-url wsdl="..."/> -->
              <xsl:value-of select="$override"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- The backend URL for this WSDL wasn't overridden in the deployment definition, so grab the backend URL from the WSDL itself. -->
              
              <xsl:variable name="remoteAddresses" select="*[local-name()='address']"/>
              <xsl:choose>
                <xsl:when test="count($remoteAddresses) = 1">
                  <xsl:value-of select="$remoteAddresses/@location"/>
                </xsl:when>
                <xsl:when test="count($remoteAddresses) &gt; 1">
                  <xsl:message terminate="yes">Encountered more than one *:address element in a wsdl:port, which makes it an invalid WSDL.</xsl:message>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'file:///dev/null'"/>
                </xsl:otherwise>
              </xsl:choose>
        
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <!-- <xsl:message>### remoteURL=<xsl:value-of select="$remoteURL"/></xsl:message> -->
        
<!--
        <redirect:write select="'zop.urls.xml'">
          <parsedURLs>
            <one><xsl:copy-of select="rx:parseURL('file:///dev/null')"/></one>
            <two><xsl:copy-of select="rx:parseURL('http://localhost/xyzzy')"/></two>
            <three><xsl:copy-of select="rx:parseURL('http://192.168.0.100:73/zither/snoeze')"/></three>
            <four><xsl:copy-of select="rx:parseURL('https://192.168.0.102/')"/></four>
            <five><xsl:copy-of select="rx:parseURL('https://192.168.0.102:98')"/></five>
          </parsedURLs>
        </redirect:write>
-->

        <!-- Pick the remote URL apart into protocol, hostname, port, and URI. -->
        <xsl:variable name="urlPieces" select="exslt:node-set(rx:parseURL($remoteURL))"/>
<!--
        <xsl:if test="contains($debugFlags, 'x')">
          <redirect:write select="'zop.before.xml'">
            <args>
              <regex><xsl:copy-of select="$regex"/></regex>
              <urlPieces><xsl:copy-of select="$urlPieces"/></urlPieces>
              <templateRemoteRewrite><xsl:copy-of select="$templateRemoteRewrite"/></templateRemoteRewrite>
            </args>
          </redirect:write>
        </xsl:if>
-->        
        <!-- <xsl:message>#### applying rx:generateNewRewriteRules-remote</xsl:message> -->
        
        <!-- Generate the local rewrite rule for this WSDL port. -->
        <xsl:apply-templates select="$templateLocalRewrite" mode="rx:generateNewRewriteRules-local">
          <xsl:with-param name="regex" select="$regex"/>
          <xsl:with-param name="bindingProtocol" select="$bindingProtocol"/>
          <xsl:with-param name="uri" select="$urlPieces/URL/URI"/>
        </xsl:apply-templates>
        
        <!-- Generate the remote rewrite rule for this WSDL port. -->
        <xsl:apply-templates select="$templateRemoteRewrite" mode="rx:generateNewRewriteRules-remote">
          <xsl:with-param name="regex" select="$regex"/>
          <xsl:with-param name="urlPieces" select="$urlPieces"/>
        </xsl:apply-templates>
        
        <!-- <xsl:message>#### finished applying rx:generateNewRewriteRules-remote</xsl:message> -->
        
      </xsl:for-each>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:generateNewRewriteRules-local">
        <xsl:with-param name="regex" select="$regex"/>
        <xsl:with-param name="bindingProtocol" select="$bindingProtocol"/>
        <xsl:with-param name="uri" select="$uri"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="ServicePortMatchRegexp" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$regex"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="LocalEndpointProtocol" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="'default'"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="LocalEndpointHostname" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="'0.0.0.0'"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="LocalEndpointPort" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="'0'"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="LocalEndpointURI" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$uri"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="UseFrontProtocol" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="'on'"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="WSDLBindingProtocol" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$bindingProtocol"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:generateNewRewriteRules-local">
        <xsl:with-param name="regex" select="$regex"/>
        <xsl:with-param name="bindingProtocol" select="$bindingProtocol"/>
        <xsl:with-param name="uri" select="$uri"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:generateNewRewriteRules-local">
    <xsl:param name="regex"/>
    <xsl:param name="bindingProtocol"/>
    <xsl:param name="uri"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <xsl:template match="/" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="rx:generateNewRewriteRules-remote">
        <xsl:with-param name="regex" select="$regex"/>
        <xsl:with-param name="urlPieces" select="$urlPieces"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="ServicePortMatchRegexp" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$regex"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="RemoteEndpointProtocol" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$urlPieces/URL/protocol"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="RemoteEndpointHostname" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$urlPieces/URL/hostname"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="RemoteEndpointPort" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$urlPieces/URL/port"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="RemoteEndpointURI" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$urlPieces/URL/URI"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="rx:generateNewRewriteRules-remote">
        <xsl:with-param name="regex" select="$regex"/>
        <xsl:with-param name="urlPieces" select="$urlPieces"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="rx:generateNewRewriteRules-remote">
    <xsl:param name="regex"/>
    <xsl:param name="urlPieces"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  

  
  
  
  <func:function name="rx:parseURL">
    <xsl:param name="URL"/>
    <func:result>
      
      <xsl:variable name="protocol" select="substring-before($URL, '://')"/>
      <xsl:variable name="remainder1" select="substring-after($URL, '://')"/>
      <xsl:variable name="hostname" select="rx:extractLeadingHostname($remainder1, 1)"/>
      <xsl:variable name="remainder2" select="substring($remainder1, string-length($hostname) + 1)"/>
      <xsl:variable name="optionalPort">
        <xsl:choose>
          <xsl:when test="starts-with($remainder2, ':')">
            <xsl:value-of select="rx:extractLeadingPort($remainder2, 1)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="''"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="URI" select="substring($remainder2, string-length($optionalPort) + 1)"/>
      <xsl:variable name="port">
        <xsl:choose>
          <xsl:when test="string-length($optionalPort) &gt; 1">
            <xsl:value-of select="substring($optionalPort, 2)"/>
          </xsl:when>
          <xsl:when test="$protocol = 'http'">
            <xsl:value-of select="'80'"/>
          </xsl:when>
          <xsl:when test="$protocol = 'https'">
            <xsl:value-of select="'443'"/>
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      
      <URL>
        <fullURL><xsl:value-of select="$URL"/></fullURL>
        <protocol><xsl:value-of select="$protocol"/></protocol> <!-- 'file', 'http', ... -->
        <hostname><xsl:value-of select="$hostname"/></hostname> <!-- isswdatapower.rtp.raleigh.ibm.com, 192.168.0.100, ... -->
        <port><xsl:value-of select="$port"/></port> <!-- 80, 443, etc -->
        <URI><xsl:value-of select="$URI"/></URI> <!-- /... -->
      </URL>
    </func:result>
  </func:function>
  
  <!--
    Return the hostname, delimited by the ':' or '/'.  The partial URL is a proper URL without
    the leading protocol:// portion.  In other words, 'hostname:80/URI' or 'hostname/URI'.  The
    index is how far into the string we are checking, beginning with 1.
  -->
  <func:function name="rx:extractLeadingHostname">
    <xsl:param name="partialURL"/>
    <xsl:param name="index"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="$index &lt;= string-length($partialURL)">
          <xsl:variable name="chr" select="substring($partialURL, $index, 1)"/>
          <xsl:choose>
            <xsl:when test="$chr = ':' or $chr = '/'">
              <!-- Return the the hostname preceding the ':' or '/' -->
              <xsl:value-of select="substring($partialURL, 1, $index - 1)"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Check the next character. -->
              <xsl:value-of select="rx:extractLeadingHostname($partialURL, $index + 1)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- The partial URL was empty or it didn't contain either a ':' or '/' -->
          <xsl:value-of select="$partialURL"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  <!--
    Return the optional port (or an empty string), without the ':'.
  -->
  <func:function name="rx:extractLeadingPort">
    <xsl:param name="partialURL"/>
    <xsl:param name="index"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="$index &lt;= string-length($partialURL)">
          <xsl:variable name="chr" select="substring($partialURL, $index, 1)"/>
          <xsl:choose>
            <xsl:when test="$chr = '/'">
              <!-- Return the the port preceding the '/' -->
              <xsl:value-of select="substring($partialURL, 1, $index - 1)"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Check the next character. -->
              <xsl:value-of select="rx:extractLeadingPort($partialURL, $index + 1)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- The partial URL was empty or it didn't contain a '/' -->
          <xsl:value-of select="$partialURL"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  
  
  <!-- 
    Given a filename that may contain '/' file separators, return the base filename.
    For example, 'x/y/z' returns 'z'.
  -->
  <func:function name="rx:basename">
    <xsl:param name="filename"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="contains($filename, '/')">
          <xsl:value-of select="rx:basename(substring-after($filename, '/'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$filename"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>

</xsl:stylesheet>
