<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2016 IBM Corp.
  
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

  This stylesheet reformats the nested hierarchy output from a get-filestore to 
  a single level that is more conveniently processed by the forXpath task.
  
  The input looks something like this:
  
  <location del="true" list="true" log="true" name="store:" read="true" show="true" write="true">
    <file name="XSS-Patterns.xml">
      <size>1081</size>
      <modified>2015-06-16 18:54:04</modified>
    </file>
    <directory audit="true" log="true" name="store:/dp" write="true">
      <file name="decrypt-ebms.xsl">
        <size>8914</size>
        <modified>2015-06-16 18:54:04</modified>
      </file>
      <file name="make-saml-assertion.xsl">
        <size>25442</size>
        <modified>2015-06-16 18:54:04</modified>
      </file>
    </directory>
    <file name="tspm-aaa-xacml-binding-rtss.xsl">
      <size>42816</size>
      <modified>2015-06-16 18:54:04</modified>
    </file>
  </location>
  
  The output is similar, and somewhat simplified, and it is reduced to a single level:
  
  <downloadFilestore>
    <directory>/monitor</directory>
    <file>/monitor/mgmtInterfaceService_Format_Response.xsl</file>
    <directory>/redteam</directory>
    <directory>/redteam/deployStuff</directory>
    <file>/redteam/deployStuff/aaaInfo.xml</file>
    <file>/redteam/deployStuff/createErrorMsg.js</file>
  <downlodFilestore>

-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:local="urn:local:function"
  xmlns:mgmt="http://www.datapower.com/schemas/management">


  <xsl:variable name="filestore" select="string(/location/@name)"/> <!-- e.g. local: -->
  
  
  <xsl:template match="/">
    <xsl:apply-templates select="node()"/>
  </xsl:template>
  
  <xsl:template match="location">
    <xsl:element name="downloadFilestore">
      
      <xsl:for-each select="directory">
        <xsl:sort select="@name"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="file">
        <xsl:sort select="@name"/>
        <xsl:apply-templates select="." mode="location">
          <xsl:with-param name="dir" select="'/'"/>
        </xsl:apply-templates>
      </xsl:for-each>
      
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="directory">
    
    <xsl:if test="* and not(starts-with(@name, 'store:/dp') or starts-with(@name, 'store:/meta') or starts-with(@name, 'store:/policies/mappings'))">
      
      <xsl:variable name="dir" select="substring-after(@name, $filestore)"/>
      <xsl:element name="directory">
        <xsl:value-of select="$dir"/>
      </xsl:element>
      
      <xsl:for-each select="directory">
        <xsl:sort select="@name"/>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="file">
        <xsl:sort select="@name"/>
        <xsl:apply-templates select="." mode="location">
          <xsl:with-param name="dir" select="concat($dir, '/')"/>
        </xsl:apply-templates>
      </xsl:for-each>
      
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="file" mode="location">
    <xsl:param name="dir"/>
    
    <xsl:element name="file">
      <xsl:value-of select="concat($dir, @name)"/>
    </xsl:element>
    
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:apply-templates select="@* | node()"/>
  </xsl:template>
  
  <xsl:template match="@* | text() | comment() | processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>
  
</xsl:stylesheet>
