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

  This stylesheet generates an Ant script that will upload certificate files to DataPower and create
  Crypto Certificate objects to wrap those files.
  
  This stylesheet is meant to be included by valcred-to-ant.xsl and idcred-to-ant.xsl.
  
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
  
  
  
  <xsl:template match="dcm:certificate[@file != '']" mode="processDefinition">
    
    <xsl:variable name="fileBasename" select="dtat:basename(@file)"/>
    
    <xsl:variable name="filestore">
      <xsl:choose>
        <xsl:when test="starts-with(@file, 'pubcert:')">
          <xsl:value-of select="'pubcert:'"/>
        </xsl:when>
        <xsl:when test="starts-with(@file, 'sharedcert:')">
          <xsl:value-of select="'sharedcert:'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'cert:'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="objname">
      <xsl:choose>
        <xsl:when test="@name != ''">
          
          <!-- Use the specified name. -->
          <xsl:value-of select="@name"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <!-- Base the name of the Crypto Certificate object on the filename. -->
          <xsl:value-of select="dtat:filenameToObjectName($fileBasename)"/>
          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- Generate the content of a SetConfig to create/overwrite the Crypto Certificate object. -->
    <xsl:variable name="config">
      <xsl:element name="CryptoCertificate">
        <xsl:attribute name="name">
          <xsl:value-of select="$objname"/>
        </xsl:attribute>
        
        <xsl:element name="mAdminState">enabled</xsl:element>
        
        <xsl:element name="Filename">
          <xsl:value-of select="concat($filestore, '///', $fileBasename)"/>
        </xsl:element>
        
        <xsl:element name="Password">
          <xsl:choose>
            <xsl:when test="@password and @prompt">
              
              <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element may have password="..." or prompt="true/false", but not both.</xsl:element>
              
            </xsl:when>
            <xsl:when test="@password">
              
              <!-- Use the supplied password. -->
              <xsl:value-of select="@password"/>
              
            </xsl:when>
            <xsl:when test="@prompt = 'true' or @prompt = 'TRUE' or @prompt = 'yes' or @prompt = 'YES' or @prompt = 'on' or @prompt = 'ON' or @prompt = '1'">
              
              <!-- Use the password that will be provided by the user in an ant property. -->
              <xsl:value-of select="'${certpass}'"/>
              
            </xsl:when>
            <xsl:otherwise>
              
              <!-- Neither password="..." or prompt="true/false" is present, so there isn't a password associated with this certificate. -->
              
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        
        <xsl:element name="PasswordAlias">off</xsl:element>
        
        <xsl:element name="IgnoreExpiration">
          <xsl:choose>
            <xsl:when test="@ignoreexpiration = 'true' or @ignoreexpiration = 'TRUE' or @ignoreexpiration = 'yes' or @ignoreexpiration = 'YES' or @ignoreexpiration = 'on' or @ignoreexpiration = 'ON' or @ignoreexpiration = '1'">
              <xsl:value-of select="'on'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'off'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        
      </xsl:element>
    </xsl:variable>
    
    <!-- Generate the ant script content to create the Crypto Certificate object on DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="local">
        <xsl:attribute name="name">dir-upload</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">response-create</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">success-create</xsl:attribute>
      </xsl:element>
      
      <xsl:if test="@prompt = 'true' or @prompt = 'TRUE' or @prompt = 'yes' or @prompt = 'YES' or @prompt = 'on' or @prompt = 'ON' or @prompt = '1'">
        
        <!-- Prompt the user for the password for this certificate. -->
        
        <xsl:element name="local">
          <xsl:attribute name="name">certpass</xsl:attribute>
        </xsl:element>
        
        <xsl:element name="input">
          <xsl:attribute name="message">Please enter the password for certificate <xsl:value-of select="@file"/> : </xsl:attribute>
          <xsl:attribute name="addproperty">certpass</xsl:attribute>
          <xsl:element name="handler">
            <xsl:attribute name="type">secure</xsl:attribute>
          </xsl:element>
        </xsl:element>
        
      </xsl:if>
      
      <xsl:if test="not(starts-with(@file, 'pubcert:') or starts-with(@file, 'sharedcert:') or starts-with(@file, 'cert:'))">
        
        <xsl:element name="dirname">
          <xsl:attribute name="file"><xsl:value-of select="@file"/></xsl:attribute>
          <xsl:attribute name="property">dir-upload</xsl:attribute>
        </xsl:element>

        <xsl:element name="dpupload">
          <xsl:attribute name="dir">${dir-upload}</xsl:attribute>
          <xsl:attribute name="target"><xsl:value-of select="concat($filestore, '///')"/></xsl:attribute>
          <xsl:attribute name="domain">${domain}</xsl:attribute>
          <xsl:attribute name="url">https://${host}:${port}/service/mgmt/current</xsl:attribute>
          <xsl:attribute name="uid">${uid}</xsl:attribute>
          <xsl:attribute name="pwd">${pwd}</xsl:attribute>
          <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
          <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
          <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
          <xsl:element name="include">
            <xsl:attribute name="name"><xsl:value-of select="$fileBasename"/></xsl:attribute>
          </xsl:element>
        </xsl:element>
        
      </xsl:if>
      
      <xsl:element name="wdp">
        <xsl:attribute name="operation">SetConfig</xsl:attribute>
        <xsl:attribute name="successprop">success-create</xsl:attribute>
        <xsl:attribute name="responseprop">response-create</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
        <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
        
        <xsl:element name="config">
          <xsl:copy-of select="$config"/>
        </xsl:element>
        
        <xsl:element name="hostname">${host}</xsl:element>
        <xsl:element name="uid">${uid}</xsl:element>
        <xsl:element name="pwd">${pwd}</xsl:element>
        <xsl:element name="port">${port}</xsl:element>
        <xsl:element name="domain">${domain}</xsl:element>
      </xsl:element>
      
      <xsl:element name="if">
        <xsl:element name="and">
          
          <xsl:element name="isset">
            <xsl:attribute name="property">success-create</xsl:attribute>
          </xsl:element>
          <xsl:element name="equals">
            <xsl:attribute name="arg1">${success-create}</xsl:attribute>
            <xsl:attribute name="arg2">true</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
        <xsl:element name="then">
          
          <xsl:element name="echo">Created the Crypto Certificate object <xsl:value-of select="$objname"/>.</xsl:element>
          
        </xsl:element>
        <xsl:element name="else">
          
          <xsl:element name="echo">Raw response for creating crypto certificate: ${response-create}</xsl:element>
          <xsl:element name="fail">
            <xsl:attribute name="message">Failed to create the Crypto Certficate object <xsl:value-of select="$objname"/>.</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:certificate[@file='' and @name='']" mode="processDefinition">
    <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element must have a file="..." or name="..." attribute.</xsl:element>
  </xsl:template>
  
  
  
  <xsl:template match="dcm:key[@file != '']" mode="processDefinition">
    
    <xsl:variable name="fileBasename" select="dtat:basename(@file)"/>
    
    <xsl:variable name="filestore">
      <xsl:choose>
        <xsl:when test="starts-with(@file, 'pubcert:')">
          <xsl:value-of select="'pubcert:'"/>
        </xsl:when>
        <xsl:when test="starts-with(@file, 'sharedcert:')">
          <xsl:value-of select="'sharedcert:'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'cert:'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="objname">
      <xsl:choose>
        <xsl:when test="@name != ''">
          
          <!-- Use the specified name. -->
          <xsl:value-of select="@name"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <!-- Base the name of the Crypto Key object on the filename. -->
          <xsl:value-of select="dtat:filenameToObjectName($fileBasename)"/>
          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- Generate the content of a SetConfig to create/overwrite the Crypto Key object. -->
    <xsl:variable name="config">
      <xsl:element name="CryptoKey">
        <xsl:attribute name="name">
          <xsl:value-of select="$objname"/>
        </xsl:attribute>
        
        <xsl:element name="mAdminState">enabled</xsl:element>
        
        <xsl:element name="Filename">
          <xsl:value-of select="concat($filestore, '///', $fileBasename)"/>
        </xsl:element>
        
        <xsl:element name="Password">
          <xsl:choose>
            <xsl:when test="@password and @prompt">
              
              <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element may have password="..." or prompt="true/false", but not both.</xsl:element>
              
            </xsl:when>
            <xsl:when test="@password">
              
              <!-- Use the supplied password. -->
              <xsl:value-of select="@password"/>
              
            </xsl:when>
            <xsl:when test="@prompt = 'true' or @prompt = 'TRUE' or @prompt = 'yes' or @prompt = 'YES' or @prompt = 'on' or @prompt = 'ON' or @prompt = '1'">
              
              <!-- Use the password that will be provided by the user in an ant property. -->
              <xsl:value-of select="'${keypass}'"/>
              
            </xsl:when>
            <xsl:otherwise>
              
              <!-- Neither password="..." or prompt="true/false" is present, so there isn't a password associated with this key. -->
              
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        
        <xsl:element name="PasswordAlias">off</xsl:element>
        
      </xsl:element>
    </xsl:variable>
    
    <!-- Generate the ant script content to create the Crypto Key object on DataPower. -->
    <xsl:element name="sequential">
      
      <xsl:element name="local">
        <xsl:attribute name="name">dir-upload</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">response-create</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">success-create</xsl:attribute>
      </xsl:element>
      
      <xsl:if test="@prompt = 'true' or @prompt = 'TRUE' or @prompt = 'yes' or @prompt = 'YES' or @prompt = 'on' or @prompt = 'ON' or @prompt = '1'">
        
        <!-- Prompt the user for the password for this key. -->
        
        <xsl:element name="local">
          <xsl:attribute name="name">keypass</xsl:attribute>
        </xsl:element>
        
        <xsl:element name="input">
          <xsl:attribute name="message">Please enter the password for key <xsl:value-of select="@file"/> : </xsl:attribute>
          <xsl:attribute name="addproperty">keypass</xsl:attribute>
          <xsl:element name="handler">
            <xsl:attribute name="type">secure</xsl:attribute>
          </xsl:element>
        </xsl:element>
        
      </xsl:if>
      
      <xsl:if test="not(starts-with(@file, 'pubcert:') or starts-with(@file, 'sharedcert:') or starts-with(@file, 'cert:'))">
        
        <xsl:element name="dirname">
          <xsl:attribute name="file"><xsl:value-of select="@file"/></xsl:attribute>
          <xsl:attribute name="property">dir-upload</xsl:attribute>
        </xsl:element>
        
        <xsl:element name="dpupload">
          <xsl:attribute name="dir">${dir-upload}</xsl:attribute>
          <xsl:attribute name="target"><xsl:value-of select="concat($filestore, '///')"/></xsl:attribute>
          <xsl:attribute name="domain">${domain}</xsl:attribute>
          <xsl:attribute name="url">https://${host}:${port}/service/mgmt/current</xsl:attribute>
          <xsl:attribute name="uid">${uid}</xsl:attribute>
          <xsl:attribute name="pwd">${pwd}</xsl:attribute>
          <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
          <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
          <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
          <xsl:element name="include">
            <xsl:attribute name="name"><xsl:value-of select="$fileBasename"/></xsl:attribute>
          </xsl:element>
        </xsl:element>
        
      </xsl:if>
        
      <xsl:element name="wdp">
        <xsl:attribute name="operation">SetConfig</xsl:attribute>
        <xsl:attribute name="successprop">success-create</xsl:attribute>
        <xsl:attribute name="responseprop">response-create</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>
        <xsl:attribute name="capturesoma">${capturesoma}</xsl:attribute>
        
        <xsl:element name="config">
          <xsl:copy-of select="$config"/>
        </xsl:element>
        
        <xsl:element name="hostname">${host}</xsl:element>
        <xsl:element name="uid">${uid}</xsl:element>
        <xsl:element name="pwd">${pwd}</xsl:element>
        <xsl:element name="port">${port}</xsl:element>
        <xsl:element name="domain">${domain}</xsl:element>
      </xsl:element>
      
      <xsl:element name="if">
        <xsl:element name="and">
          
          <xsl:element name="isset">
            <xsl:attribute name="property">success-create</xsl:attribute>
          </xsl:element>
          <xsl:element name="equals">
            <xsl:attribute name="arg1">${success-create}</xsl:attribute>
            <xsl:attribute name="arg2">true</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
        <xsl:element name="then">
          
          <xsl:element name="echo">Created the Crypto Key object <xsl:value-of select="$objname"/>.</xsl:element>
          
        </xsl:element>
        <xsl:element name="else">
          
          <xsl:element name="echo">Raw response for creating crypto key: ${response-create}</xsl:element>
          <xsl:element name="fail">
            <xsl:attribute name="message">Failed to create the Crypto Key object <xsl:value-of select="$objname"/>.</xsl:attribute>
          </xsl:element>
          
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template match="dcm:key[@file='' and @name='']" mode="processDefinition">
    <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element must have a file="..." or name="..." attribute.</xsl:element>
  </xsl:template>
  
  
  
  
</xsl:stylesheet>
