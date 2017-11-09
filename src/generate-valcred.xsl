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

  This stylesheet generates a set of Crypto Certificate objects and a Validation Credential object based on a list
  of certificate files specified in the input:
  
  <files>
    <file filenameonly="preuat_td.cer">cert:///preuat_td.cer</file>
    ...
  </files>
  
  The output always includes the configuration for a Validation Credential object, even if that object has an
  empty list of certificate objects.
  
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
  
  
  <xsl:param name="objname"/> <!-- name of the Validation Credential object -->
  <xsl:param name="ignoreexpiration"/> <!-- on or off -->
  
  
  
  <xsl:template match="/">

    <xsl:element name="config">
      <xsl:apply-templates select="files"/>
    </xsl:element>
    
  </xsl:template>
  
  
  
  <xsl:template match="files">
    
    <!-- Generate Crypto Certificate objects for each of the <file> entries. -->
    <xsl:apply-templates select="file"/>
    
    <!-- Generate a valcred object that encompasses the Crypto Certificate objects that we just defined. -->
    <xsl:element name="CryptoValCred">
      <xsl:attribute name="name"><xsl:value-of select="$objname"/></xsl:attribute>
      
      <xsl:element name="mAdminState">enabled</xsl:element>
      
      <xsl:for-each select="file">
        
        <xsl:element name="Certificate">
          <xsl:attribute name="class">CryptoCertificate</xsl:attribute>
          <xsl:value-of select="@filenameonly"/>
        </xsl:element>
        
      </xsl:for-each>
      
      <xsl:element name="CertValidationMode">legacy</xsl:element>
      
      <xsl:element name="UseCRL">on</xsl:element>
      
      <xsl:element name="RequireCRL">off</xsl:element>
      
      <xsl:element name="CRLDPHandling">ignore</xsl:element>
      
      <xsl:element name="ExplicitPolicy">off</xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  
  <xsl:template match="file">
    
    <xsl:element name="CryptoCertificate">
      <xsl:attribute name="name"><xsl:value-of select="@filenameonly"/></xsl:attribute>
      
      <xsl:element name="mAdminState">enabled</xsl:element>
      
      <xsl:element name="Filename">
        <xsl:value-of select="."/>
      </xsl:element>
      
      <xsl:element name="Password"/> <!-- Passwords are currently not supported for certificates - maybe later -->
        
      <xsl:element name="PasswordAlias">off</xsl:element>
      
      <xsl:element name="IgnoreExpiration">off</xsl:element> <!-- like passwords, ignoring expiration is also not currently supported -->
      
    </xsl:element>
  </xsl:template>
  
</xsl:stylesheet>
