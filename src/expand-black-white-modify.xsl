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

  This stylesheet replaces each <dcm:deployment-policy>/(black-list|white-list|*-property)
  with an equivalent FilteredConfig, AcceptedConfig, or ModifiedConfig element.

-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:dcm="urn:datapower:configuration:manager"
  xmlns:date="http://exslt.org/dates-and-times"
  xmlns:dyn="http://exslt.org/dynamic"
  xmlns:ebwm="urn:local-function:expand-black-white-modify"
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
  
  <xsl:template match="dcm:deployment-policy/black-list">
    <FilteredConfig><xsl:value-of select="ebwm:captureMatchingInfo(.)"/></FilteredConfig>
  </xsl:template>
  
  <xsl:template match="dcm:deployment-policy/white-list">
    <AcceptedConfig><xsl:value-of select="ebwm:captureMatchingInfo(.)"/></AcceptedConfig>
  </xsl:template>
  
  <xsl:template match="dcm:deployment-policy/add-property">
    <ModifiedConfig>
      <Match><xsl:value-of select="ebwm:captureMatchingInfo(.)"/></Match>
      <Type>add</Type>
      <Property><xsl:value-of select="@new-prop-name"/></Property>
      <Value><xsl:value-of select="@new-prop-value"/></Value>
    </ModifiedConfig>
  </xsl:template>
  
  <xsl:template match="dcm:deployment-policy/delete-property">
    <ModifiedConfig>
      <Match><xsl:value-of select="ebwm:captureMatchingInfo(.)"/></Match>
      <Type>delete</Type>
      <Property/>
      <Value/>
    </ModifiedConfig>
  </xsl:template>
  
  <xsl:template match="dcm:deployment-policy/modify-property">
    <ModifiedConfig>
      <Match><xsl:value-of select="ebwm:captureMatchingInfo(.)"/></Match>
      <Type>change</Type>
      <Property/>
      <Value><xsl:value-of select="@new-prop-value"/></Value>
    </ModifiedConfig>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|text()|comment()|processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>
  


  
  <!-- 
    This function picks out these attributes and combines them to return the usual
    "*/*/*xxx/yyy?Name=zzz..." permission string:
    @device - Device IP address - defaults to *
    @domain - defaults to *
    @type - type of DP object to match - see dmResourceType in store://schemas/xml-mgmt.xsd
    for a comprehensive list.  default is *, implying all types of DP objects.
    @name - PCRE for name(s) of object(s) to match - default to .* for all object names
    @prop-name - optional - name of property to check to determine whether an object matches 
    the criteria.  No default value.
    @prop-value - goes with @prop-name - PCRE to match the value of the specified DP object
    property.  defaults to .*
  -->
  <func:function name="ebwm:captureMatchingInfo">
    <xsl:param name="el"/>
    <!-- Capture the various attributes, providing default values as appropriate. -->
    <xsl:variable name="deviceid">
      <xsl:choose>
        <xsl:when test="$el/@device!=''">
          <xsl:value-of select="$el/@device"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'*'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="domain">
      <xsl:choose>
        <xsl:when test="$el/@domain!=''">
          <xsl:value-of select="$el/@domain"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'*'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="$el/@type!=''">
          <xsl:value-of select="$el/@type"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'*'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="$el/@name!=''">
          <xsl:value-of select="$el/@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'.*'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="prop-name" select="$el/@prop-name"/>
    <xsl:variable name="prop-value">
      <xsl:choose>
        <xsl:when test="$el/@prop-value!=''">
          <xsl:value-of select="$el/@prop-value"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'.*'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="firstPart" select="concat($deviceid, '/', $domain, '/', $type, '?Name=', $name)"/>
    
    <xsl:variable name="secondPart">
      <xsl:choose>
        <xsl:when test="$prop-name!=''">
          <xsl:value-of select="concat('&amp;Property=', $prop-name, '&amp;Value=', $prop-value)"/>
        </xsl:when>
        <xsl:when test="$prop-name=''">
          <xsl:value-of select="concat('&amp;Value=', $prop-value)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    
    <func:result select="concat($firstPart, $secondPart)"/>
  </func:function>
  
  
  
</xsl:stylesheet>
