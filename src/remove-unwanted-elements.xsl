<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2014, 2015 IBM Corp.
  
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
  
  Ditto deviceid="...", which is useful, for example, when specifying a key/cert
  pair for a particular idcred, or defining a host alias for an ethernet port.
  
  In addition to all that, it resolves any <dcm:include href="..."/> elements.
  The included file may have any root element you like.  The root element of
  the included file may be <dcm:wrapper>.  In this case the contents of the
  <dcm:wrapper> is kept, ignoring the wrapper element itself.
  
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
  <xsl:param name="param2" select="''"/> <!-- desired device (e.g. '', dp01, prod1, prod2) -->
  
  
  <xsl:template match="/">
    <xsl:variable name="resolveIncludes">
      <xsl:apply-templates select="." mode="handleIncludes"/>
    </xsl:variable>
    <xsl:variable name="filtered">
      <xsl:apply-templates select="exslt:node-set($resolveIncludes)" mode="filter"/>
    </xsl:variable>
    <xsl:copy-of select="exslt:node-set($filtered)"/>
  </xsl:template>


  <!-- *************************************************************************************** -->


  <xsl:template match="/" mode="handleIncludes">
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="handleIncludes"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="dcm:include[@href!='']" mode="handleIncludes">
    <xsl:apply-templates select="document(@href)" mode="handleIncludes"/>
  </xsl:template>
  
  <xsl:template match="dcm:include" mode="handleIncludes">
    <xsl:element name="cman:error">The &lt;dcm:include&gt; element must have a href attribute that points to some file to include.</xsl:element>
  </xsl:template>
  
  <xsl:template match="dcm:wrapper" mode="handleIncludes">
    <!-- Implicitly remove the dcm:wrapper element, keeping only its children. -->
    <xsl:apply-templates select="node()" mode="handleIncludes"/>
  </xsl:template>
  
  <xsl:template match="*" mode="handleIncludes">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="handleIncludes"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()" mode="handleIncludes">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  <!-- *************************************************************************************** -->
  
  
  <xsl:template match="/" mode="filter">
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="filter"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*[@environment and @deviceid]" mode="filter">
    <!-- <xsl:message>### e+d : <xsl:value-of select="name()"/></xsl:message> -->
    <xsl:if test="contains(@environment, $param1) and contains(@deviceid, $param2)">
      <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="filter"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[@environment and not(@deviceid)]" mode="filter">
    <!-- <xsl:message>### e : <xsl:value-of select="name()"/></xsl:message> -->
    <xsl:if test="contains(@environment, $param1)">
      <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="filter"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[not(@environment) and @deviceid]" mode="filter">
    <!-- <xsl:message>### d : <xsl:value-of select="name()"/></xsl:message> -->
    <xsl:if test="contains(@deviceid, $param2)">
      <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="filter"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[not(@environment) and not(@deviceid)]" mode="filter">
    <!-- <xsl:message>### neither : <xsl:value-of select="name()"/></xsl:message> -->
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="filter"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@environment | @deviceid" mode="filter"/> <!-- drop these attributes -->
    
  <xsl:template match="@* | text() | comment() | processing-instruction()" mode="filter">
    <xsl:copy-of select="."/>
  </xsl:template>
    
  
</xsl:stylesheet>
