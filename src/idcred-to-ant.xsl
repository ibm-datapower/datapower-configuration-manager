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

  This stylesheet generates an Ant script that will upload key and certificate files to DataPower, create
  Crypto Key/Certificate objects to wrap those files, and then create an ID Credential object to
  wrap those objects.
  
  The input to the stylesheet is a dcm:definition in this pattern:
  
  <dcm:definition>
    <dcm:idcred name="...">
      <dcm:key [file="..."] [name="..."] [ (password="..." | prompt="t/f") ]/>
      <dcm:certificate [file="..."] [name="..."] [ (password="..." | prompt="t/f") ] [ignoreexpiration="t/f"]/>
    </dcm:idcred>
  </dcm:definition>
  
  The "template-file" stylesheet parameter is the name of an Ant script template file (e.g. _quickie.template.ant.xml).
  
-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:dcm="urn:datapower:configuration:manager"
  xmlns:date="http://exslt.org/dates-and-times"
  xmlns:dtat="urn:local-function:definition-to-ant-tasks"
  xmlns:dyn="http://exslt.org/dynamic"
  xmlns:exslt="http://exslt.org/common"
  xmlns:func="http://exslt.org/functions"
  xmlns:local="urn:local:function"
  xmlns:mgmt="http://www.datapower.com/schemas/management"
  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:str="http://exslt.org/strings"
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">
  
  
  <xsl:param name="template-file"/> <!-- filename of the Ant script template -->
  
  <xsl:include href="util-to-ant.xsl"/>
  <xsl:include href="util-merge-template.xsl"/>
  <xsl:include href="util-mutate-names.xsl"/>
  
  
  <xsl:template match="/">

    <!-- Load the Ant script template. -->
    <xsl:variable name="template" select="document($template-file)"/>
    <xsl:choose>
      <xsl:when test="$template/project">

        <!-- Process the dcm:definition input. -->
        <xsl:variable name="results" select="exslt:node-set(local:processDefinition(.))"/>
        <xsl:choose>
          <xsl:when test="$results//dcm:error">
            
            <!-- Show the errors and terminate. -->
            <xsl:for-each select="$results//dcm:error">
              <xsl:message>   <xsl:value-of select="."/></xsl:message>
            </xsl:for-each>
            <xsl:message terminate="yes">Failed to generate internal template due to errors listed above.</xsl:message>
            
          </xsl:when>
          <xsl:when test="$results/generated/antscript/*">
            
            <!-- Successfully generated ant script content without encountering any errors. Merge that content into the template. -->
            <xsl:copy-of select="dtat:mergeTemplate($template, $results)"/>
            
          </xsl:when>
          <xsl:otherwise>
            
            <xsl:message terminate="yes">Odd.  Failed to generate an internal template and there isn't any apparent reason.</xsl:message>
            
          </xsl:otherwise>
        </xsl:choose>
        
      </xsl:when>
      <xsl:otherwise>
        
        <xsl:message terminate="yes">Failed to load the Ant script template <xsl:value-of select="$template-file"/>!</xsl:message>
        
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <!-- 
    Generate Ant script content based on the supplied dcm:definition.  The result may contain
    dcm:error elements that describe various errors in the input definition.
  -->
  <func:function name="local:processDefinition">
    <xsl:param name="dcmdef"/>
    <func:result>
      
      <xsl:element name="generated">
        <xsl:element name="antscript">

          <xsl:choose>
            <xsl:when test="$dcmdef/dcm:definition">
              
              <xsl:choose>
                <xsl:when test="$dcmdef/dcm:definition/dcm:idcred">
                  
                  <xsl:for-each select="$dcmdef/dcm:definition/dcm:idcred">
                  
                    <xsl:variable name="keys" select="count(dcm:key)"/>
                    <xsl:variable name="certs" select="count(dcm:certificate)"/>
                    
                    <xsl:choose>
                      <xsl:when test="$certs = 1 and $keys = 1">
                        
                        <!-- Generate ant script content for this idcred -->
                        <xsl:apply-templates select="." mode="processDefinition"/>
                        
                      </xsl:when>
                      <xsl:otherwise>

                        <xsl:if test="$certs != 1">
                          <xsl:element name="dcm:error">The <xsl:value-of select="concat(name(), '/', @name)"/> element must contain exactly one dcm:certificate element.</xsl:element>
                        </xsl:if>
                        <xsl:if test="$keys != 1">
                          <xsl:element name="dcm:error">The <xsl:value-of select="concat(name(), '/', @name)"/> element must contain exactly one dcm:key element.</xsl:element>
                        </xsl:if>
                        
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:for-each>

                </xsl:when>
                <xsl:otherwise>
                  
                  <xsl:element name="dcm:error">This file doesn't contain a dcm:idcred element!</xsl:element>
                  
                </xsl:otherwise>
              </xsl:choose>
              
            </xsl:when>
            <xsl:otherwise>
              
              <xsl:element name="dcm:error">This file doesn't contain a dcm:definition element!</xsl:element>
              
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:element>
      
    </func:result>
  </func:function>
  
  
  <xsl:template match="dcm:idcred[@name != '']" mode="processDefinition">
<!--    
    <xsl:element name="sequential">
      <xsl:element name="echo">
        <xsl:value-of select="'###'"/>
      </xsl:element>
      <xsl:element name="echoproperties"/>
      <xsl:element name="echo">
        <xsl:value-of select="'###'"/>
      </xsl:element>
    </xsl:element>
-->    
    <!-- Generate ant script content to upload the key file and create a Crypto Key wrapper for it. -->
    <xsl:apply-templates select="dcm:key" mode="processDefinition"/>
    
    <!-- Generate ant script content to upload the certificate file and create a Crypto Certificate wrapper for it. -->
    <xsl:apply-templates select="dcm:certificate" mode="processDefinition"/>
    
    <!-- Generate the content of a SetConfig to create/overwrite the Validation Credential object. -->
    <xsl:variable name="config">
      <xsl:element name="CryptoIdentCred">
        <xsl:attribute name="name">
          <xsl:value-of select="@name"/>
        </xsl:attribute>
        
        <xsl:element name="mAdminState">enabled</xsl:element>
        
        <xsl:element name="Key">
          <xsl:attribute name="class">CryptoKey</xsl:attribute>
          <xsl:choose>
            <xsl:when test="dcm:key/@name != ''">
              
              <!-- Use the specified name. -->
              <xsl:value-of select="dcm:key/@name"/>
              
            </xsl:when>
            <xsl:otherwise>
              
              <!-- Base the name of the Crypto Key object on the filename. -->
              <xsl:value-of select="dtat:filenameToObjectName(dtat:basename(dcm:key/@file))"/>
              
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        
        <xsl:element name="Certificate">
          <xsl:attribute name="class">CryptoCertificate</xsl:attribute>
          <xsl:choose>
            <xsl:when test="dcm:certificate/@name != ''">
              
              <!-- Use the specified name. -->
              <xsl:value-of select="dcm:certificate/@name"/>
              
            </xsl:when>
            <xsl:otherwise>
              
              <!-- Base the name of the Crypto Certificate object on the filename. -->
              <xsl:value-of select="dtat:filenameToObjectName(dtat:basename(dcm:certificate/@file))"/>
              
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        
      </xsl:element>
      
    </xsl:variable>
    
    <!-- Generate the ant script content to create the idcred object on DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="local">
        <xsl:attribute name="name">response-create</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">success-create</xsl:attribute>
      </xsl:element>
      
      <xsl:element name="wdp">
        <xsl:attribute name="operation">SetConfig</xsl:attribute>
        <xsl:attribute name="successprop">success-create</xsl:attribute>
        <xsl:attribute name="responseprop">response-create</xsl:attribute>
        
        <xsl:element name="config">
          <xsl:copy-of select="$config"/>
        </xsl:element>
        
        <xsl:element name="hostname">${host}</xsl:element>
        <xsl:element name="uid">${uid}</xsl:element>
        <xsl:element name="pwd">${pwd}</xsl:element>
        <xsl:element name="port">${port}</xsl:element>
        <xsl:element name="domain">${domain}</xsl:element>
      </xsl:element>
      
      <xsl:element name="if">
        <xsl:element name="and">
          
          <xsl:element name="isset">
            <xsl:attribute name="property">success-create</xsl:attribute>
          </xsl:element>
          <xsl:element name="equals">
            <xsl:attribute name="arg1">${success-create}</xsl:attribute>
            <xsl:attribute name="arg2">true</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
        <xsl:element name="then">
          
          <xsl:element name="echo">Created the ID Credential object <xsl:value-of select="@name"/>.</xsl:element>
          
        </xsl:element>
        <xsl:element name="else">
          
          <xsl:element name="echo">Raw response for creating id cred: ${response-create}</xsl:element>
          <xsl:element name="fail">
            <xsl:attribute name="message">Failed to create the ID Credential object <xsl:value-of select="@name"/>.</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:idcred" mode="processDefinition">
    <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element must have a name="..." attribute.</xsl:element>
  </xsl:template>
  
  
</xsl:stylesheet>
