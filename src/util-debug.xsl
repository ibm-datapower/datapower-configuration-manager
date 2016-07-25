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

  This stylesheet contains utilities useful for debugging.

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
  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:str="http://exslt.org/strings"
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">

  <xsl:output method="xml" encoding="UTF-8"/>
  
  
  <xsl:variable name="nl">
    <xsl:text xml:space="preserve">

    </xsl:text>
  </xsl:variable>
  


  <func:function name="local:dumpNodesetAsText">
    <xsl:param name="el"/>
    <xsl:param name="depth" select="1000"/>
    <func:result>
      <xsl:apply-templates select="$el" mode="local:dumpNodesetAsText">
        <xsl:with-param name="depth" select="$depth"/>
        <xsl:with-param name="prefix" select="''"/>
      </xsl:apply-templates>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:dumpNodesetAsText">
    <xsl:param name="depth"/>
    <xsl:param name="prefix"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat($nl, $prefix, 'document', $nl)"/>
      <xsl:apply-templates select="node()" mode="local:dumpNodesetAsText">
        <xsl:with-param name="depth" select="$depth"/>
        <xsl:with-param name="prefix" select="concat($prefix, '  ')"/>
      </xsl:apply-templates>
      <xsl:value-of select="$nl"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="local:dumpNodesetAsText">
    <xsl:param name="depth"/>
    <xsl:param name="prefix"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat($prefix, 'element ', name(), ' ')"/>
      <xsl:for-each select="@*">
        <xsl:value-of select="concat(name(), '=', string(), ' ')"/>
      </xsl:for-each>
      <xsl:value-of select="$nl"/>
      <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="local:dumpNodesetAsText">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="prefix" select="concat($prefix, '  ')"/>
      </xsl:apply-templates>
      <xsl:value-of select="$nl"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="local:dumpNodesetAsText">
    <xsl:param name="depth"/>
    <xsl:param name="prefix"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat($prefix, 'text ', normalize-space())"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="comment()" mode="local:dumpNodesetAsText">
    <xsl:param name="depth"/>
    <xsl:param name="prefix"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat($prefix, 'comment ', .)"/>
      <xsl:copy/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="processing-instruction()" mode="local:dumpNodesetAsText">
    <xsl:param name="depth"/>
    <xsl:param name="prefix"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat($prefix, 'processing-instruction ', .)"/>
      <xsl:copy/>
    </xsl:if>
  </xsl:template>
  
  
</xsl:stylesheet>
