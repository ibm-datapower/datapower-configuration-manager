<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2014,2015 IBM Corp.
  
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

  This stylesheet generates an Ant script that will upload files to DataPower
  
  The input to the stylesheet is a dcm:definition in this pattern:
  
  <dcm:definition>
    <dcm:upload local="..." remotedir="..."/>
    ...
  </dcm:definition>
  
  The local="..." attribute is the local path and the remote="..." attribute is the complete filename on DataPower.
  
  For example:
  
    <dcm:definition>
      <dcm:upload local="cert/datapower/ltpa/ltpa_sit" remotedir="cert:///"/>
      <dcm:upload local="dcm/src/xyz.xsl" remotedir="local:///a/b/c"/>
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
                <xsl:when test="$dcmdef/dcm:definition/dcm:upload">
                  
                  <xsl:apply-templates select="$dcmdef/dcm:definition/dcm:upload" mode="processDefinition"/>
                  
                </xsl:when>
                <xsl:otherwise>
                  
                  <xsl:element name="dcm:error">This file doesn't contain an dcm:definition/dcm:upload elements!</xsl:element>
                  
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
  
  
  <xsl:template match="dcm:upload[@local != '' and @remotedir != '']" mode="processDefinition">
    
    <xsl:variable name="localBasename" select="dtat:basename(@local)"/>
    
    <!-- Generate the ant script content to upload the file to DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="local">
        <xsl:attribute name="name">dir-upload</xsl:attribute>
      </xsl:element>
      
      <xsl:element name="dirname">
        <xsl:attribute name="file"><xsl:value-of select="@local"/></xsl:attribute>
        <xsl:attribute name="property">dir-upload</xsl:attribute>
      </xsl:element>
      
      <xsl:element name="dpupload">
        <xsl:attribute name="dir">${dir-upload}</xsl:attribute>
        <xsl:attribute name="target"><xsl:value-of select="@remotedir"/></xsl:attribute>
        <xsl:attribute name="domain">${domain}</xsl:attribute>
        <xsl:attribute name="url">https://${host}:${port}/service/mgmt/current</xsl:attribute>
        <xsl:attribute name="uid">${uid}</xsl:attribute>
        <xsl:attribute name="pwd">${pwd}</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
        <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
        <xsl:element name="include">
          <xsl:attribute name="name"><xsl:value-of select="$localBasename"/></xsl:attribute>
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:upload[@file != '' and @remotedir != '']" mode="processDefinition">
    
    <!-- Generate the ant script content to upload the file to DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="dpupload">
        <xsl:attribute name="file"><xsl:value-of select="@file"/></xsl:attribute>
        <xsl:attribute name="target"><xsl:value-of select="@remotedir"/></xsl:attribute>
        <xsl:attribute name="domain">${domain}</xsl:attribute>
        <xsl:attribute name="url">https://${host}:${port}/service/mgmt/current</xsl:attribute>
        <xsl:attribute name="uid">${uid}</xsl:attribute>
        <xsl:attribute name="pwd">${pwd}</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
        <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:upload[@localdir != '' and @remotedir != '' and @match != '']" mode="processDefinition">
    
    <!-- Generate the ant script content to upload the file to DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="dpupload">
        <xsl:attribute name="dir"><xsl:value-of select="@localdir"/></xsl:attribute>
        <xsl:attribute name="target"><xsl:value-of select="@remotedir"/></xsl:attribute>
        <xsl:attribute name="domain">${domain}</xsl:attribute>
        <xsl:attribute name="url">https://${host}:${port}/service/mgmt/current</xsl:attribute>
        <xsl:attribute name="uid">${uid}</xsl:attribute>
        <xsl:attribute name="pwd">${pwd}</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
        <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
        <xsl:element name="include">
          <xsl:attribute name="name"><xsl:value-of select="@match"/></xsl:attribute>
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:upload" mode="processDefinition">
    <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element must have (local="..." or file="..." or (localdir="..." and match="...")) and remotedir="..." attributes.</xsl:element>
  </xsl:template>
  
</xsl:stylesheet>
