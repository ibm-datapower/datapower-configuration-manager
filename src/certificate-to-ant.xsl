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

  This stylesheet generates an Ant script that will upload certificate files to DataPower and create
  Crypto Certificate objects to wrap those files.
  
  The input to the stylesheet is a dcm:definition in this pattern:
  
  <dcm:definition>
    <dcm:certificate [file="..."] [name="..."] [ (password="..." | prompt="t/f") ] [ignoreexpiration="t/f"]/>
    ...
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
            
            <xsl:message terminate="yes">Odd.  Failed to generate an internal template and there isn't any apparent reason based on <xsl:value-of select="$template-file"/>.</xsl:message>
            
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
                <xsl:when test="$dcmdef/dcm:definition/dcm:certificate">
                  
                  <xsl:apply-templates select="$dcmdef/dcm:definition/dcm:certificate" mode="processDefinition"/>
                  
                </xsl:when>
                <xsl:otherwise>
                  
                  <xsl:element name="dcm:error">This file doesn't contain an dcm:definition/dcm:certificate elements!</xsl:element>
                  
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
  
  
</xsl:stylesheet>
