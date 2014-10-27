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

  This stylesheet recursively replaces each <dcm:include @href> in the 
  input with the contents of the hrefs.  It also finds each <dcm:wsdl> and
  inserts the contents of that wsdl as the contents of the <dcm:wsdl> element.

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
  xmlns:str="http://exslt.org/strings"
  xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
  xmlns:wsdlsoap11="http://schemas.xmlsoap.org/wsdl/soap/"
  xmlns:wsdlsoap12="http://schemas.xmlsoap.org/wsdl/soap12/"
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">
  
  
  <xsl:template match="/">
    <xsl:copy>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="dcm:include[@href!='']">
    <xsl:apply-templates select="document(@href)"/>
  </xsl:template>
  
  <xsl:template match="dcm:include">
    <xsl:element name="dcm:error">The &lt;dcm:include&gt; element must have a href attribute that points to some file to include.</xsl:element>
  </xsl:template>

  <xsl:template match="dcm:wrapper">
    <!-- Implicitly remove the dcm:wrapper element, keeping only its children. -->
    <xsl:apply-templates select="node()"/>
  </xsl:template>
  
  <xsl:template match="dcm:wsdl">
    <xsl:apply-templates select="." mode="gatherWSDL">
      <xsl:with-param name="keepFirstWsdlDefinition" select="true()"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <xsl:template match="dcm:wsdl" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:variable name="doc" select="document(@href)"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="$doc" mode="gatherWSDL">
        <xsl:with-param name="keepFirstWsdlDefinition" select="$keepFirstWsdlDefinition"/>
      </xsl:apply-templates>
    </xsl:copy>
    <xsl:if test="not($doc/*)">
      <xsl:message terminate="yes">Failed to find, access, or read the WSDL at URL=<xsl:value-of select="@href"/></xsl:message>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="wsdl:definitions" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:choose>
      <xsl:when test="$keepFirstWsdlDefinition">
        
        <!-- Copy the wsdl:definitions element plus its children. -->
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="gatherWSDL">
            <xsl:with-param name="keepFirstWsdlDefinition" select="false()"/>
          </xsl:apply-templates>
        </xsl:copy>
        
      </xsl:when>
      <xsl:otherwise>
        
        <!-- Copy only the wsdl:definitions element's children. -->
        <xsl:apply-templates select="node()" mode="gatherWSDL">
          <xsl:with-param name="keepFirstWsdlDefinition" select="false()"/>
        </xsl:apply-templates>
        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="wsdl:import" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:variable name="doc" select="document(@location)"/>
    <xsl:apply-templates select="$doc" mode="gatherWSDL">
      <xsl:with-param name="keepFirstWsdlDefinition" select="false()"/>
    </xsl:apply-templates>
    <xsl:if test="not($doc/*)">
      <xsl:message terminate="yes">Failed to find, access, or read the WSDL at URL=<xsl:value-of select="@location"/></xsl:message>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="wsdl:message | wsdl:operation" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <!-- We don't need any 'type' information, so truncate the WSDL at this point. -->
  </xsl:template>
  
  <xsl:template match="/" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="gatherWSDL">
        <xsl:with-param name="keepFirstWsdlDefinition" select="$keepFirstWsdlDefinition"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*|text()|processing-instruction()|comment()" mode="gatherWSDL">
        <xsl:with-param name="keepFirstWsdlDefinition" select="$keepFirstWsdlDefinition"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|processing-instruction()|comment()" mode="gatherWSDL">
    <xsl:param name="keepFirstWsdlDefinition"/>
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
</xsl:stylesheet>
