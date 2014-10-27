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

  This stylesheet expects dcm:definition input.  It extracts the deployment policies and
  writes them to the output.
  
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
  
  
  <xsl:template match="/">
    
    <xsl:element name="dcm:wrapper">
      <xsl:choose>
        <xsl:when test="definition">
          <xsl:copy-of select="/definition/dcm:definition/dcm:deployment-policy/*"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="/dcm:definition/dcm:deployment-policy/*"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
    
  </xsl:template>
  
</xsl:stylesheet>
