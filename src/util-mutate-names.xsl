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
  This stylesheet contains functions needed by multiple stylesheets.
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
  xmlns:str="http://exslt.org/strings"
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">
  
  
  
  <!-- 
    This function scrubs a supplied hostname, IP address, etc. into something suitable for
    an ANT property name.  Basically, illegal characters are turned into underscores,
    and if the name begins with a number then a prefix is prepended to the resulting
    string.
  -->
  <func:function name="dtat:nameToANTpropname">
    <xsl:param name="hostname"/>
    <func:result>
      <!-- It isn't obvious, but $, {, and } are permitted in order to facilitate nested property references. -->
      <xsl:variable name="scrubbed" select="translate($hostname, '~!@#%^&amp;*()_-+=[]|\\:&lt;&gt;,.?/', '________________________________')"/>
      <xsl:choose>
        <xsl:when test="translate(substring($scrubbed, 1, 1), '0123456789', '')=''">
          <xsl:value-of select="concat('ipaddr_', $scrubbed)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$scrubbed"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  
  <!-- 
    This function scrubs a supplied hostname, IP address, etc. into something suitable for
    an ANT property name.  Basically, illegal characters are turned into underscores,
    and if the name begins with a number then a prefix is prepended to the resulting
    string.
  -->
  <func:function name="dtat:nameToANTtargetname">
    <xsl:param name="hostname"/>
    <func:result>
      <!-- $, {, and } are removed from the scrubbed string. -->
      <xsl:variable name="scrubbed" select="translate($hostname, '~!@#%^&amp;*()_-+=[]|\\:&lt;&gt;,.?/${}', '__________________________')"/>
      <xsl:choose>
        <xsl:when test="translate(substring($scrubbed, 1, 1), '0123456789', '')=''">
          <xsl:value-of select="concat('ipaddr_', $scrubbed)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$scrubbed"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  
  <!-- 
    Given a filename that may contain '/' or '\' file separators, return the base filename.
    For example, 'x/y/z' returns 'z'.
  -->
  <func:function name="dtat:basename">
    <xsl:param name="filename"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="contains($filename, '/')">
          <xsl:value-of select="dtat:basename(substring-after($filename, '/'))"/>
        </xsl:when>
        <xsl:when test="contains($filename, '\')">
          <xsl:value-of select="dtat:basename(substring-after($filename, '\'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$filename"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>



  <!-- 
    Return a name suitable for a DataPower object, based on the supplied filename.  For example,
    'c:/dir/abc.cer' is returns 'abc.cer'. 
  -->
  <func:function name="dtat:filenameToObjectName">
    <xsl:param name="filename"/>
    <func:result select="translate(dtat:basename($filename), '~!@#%^&amp;*()_-+=[]|\\:&lt;&gt;,.?/${}', '__________________________')"/>
  </func:function>
  
  
  
</xsl:stylesheet>
