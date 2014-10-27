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

  This stylesheet scans the DCM policy and removing elements which have an
  environment="..." attribute that doesn't contain the specified value.
  
  In other words, if you have multiple dcm:environment elements with different
  environment="..." attributes (e.g. dev, qa, prod), this stylesheet will only 
  pass the one for environment="dev".
  
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
  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:str="http://exslt.org/strings"
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">
  
  
  <xsl:param name="param1" select="''"/> <!-- desired environment (e.g. '', dev, qa, prod) -->
  

  <xsl:template match="/">
    
    <xsl:choose>
      <xsl:when test="$param1 != ''">
        
        <!-- Remove all elements that have an @environment different from $environment -->
        <xsl:apply-templates select="node()"/>
        
      </xsl:when>
      <xsl:otherwise>
        
        <!-- No $environment specified on the command line so don't make any changes. -->
        <xsl:copy-of select="."/>
        
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*">
    
    <xsl:choose>
      <xsl:when test="not(@environment) or @environment = $param1">
        
        <!-- The element either has no @environment, or it has the correct environment, so copy it. -->
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>

      </xsl:when>
      <xsl:otherwise>
        
        <!-- Drop this element, it has the wrong @environment. -->
        
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="@environment"/> <!-- Drop all @environment attributes. -->
  
  <xsl:template match="@* | text() | comment() | processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
</xsl:stylesheet>
