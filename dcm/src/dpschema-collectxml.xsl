<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:dp="http://www.datapower.com/extensions"
                              xmlns:exsl="http://exslt.org/common"
                              xmlns:func="http://exslt.org/functions"
                              xmlns:is="http://anything.com/is"
                              xmlns:regexp="http://exslt.org/regular-expressions"
                              xmlns:str="http://exslt.org/strings"
                              xmlns:tns="http://www.datapower.com/schemas/management"
                              xmlns:util="http://anything.com/util"
                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                              extension-element-prefixes="dp regexp" 
                              exclude-result-prefixes="str">
  <xsl:output method="xml" indent="yes"/>


  <!-- 
    This stylesheet accepts a WSDL and grabs all the other files it includes, producing
    a set of XML that has replaced the xsd:includes with the contents of the files.
  -->
  <xsl:template match="/">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xsd:include">
    <xsl:apply-templates select="document(@schemaLocation)"/>
  </xsl:template>

  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>



