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

  This stylesheet has only the namespace for XSL itself, and needs to remain that way.
  
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common">
  

  <!--
    
    This inoffensive template adds prefix:namespace definitions to the root element of
    the document using a simple technique.
    
    1. Create a new document root element with fake attributes, one per desired namespace.
    2. Copy the specified document as the contents of this new root element.
    3. Copy the contents back out of the new root element.
    
    It seems a bit magical at first, but it simply relies on the rules of namespace affinity
    in XSLT.
    
    This stylesheet has only the XSL prefix and namespace defined so that the <newRoot>
    element isn't tainted with additional, undesired namespaces.
    
  -->
  <xsl:template match="/" mode="hoistPrefixDefinitions">
    <xsl:param name="definitions"/>
    
    <xsl:variable name="newRoot">
      <xsl:element name="newRoot">
        
        <xsl:for-each select="$definitions//definition[@prefix!='']">
          <xsl:attribute name="{concat(@prefix, ':dummy')}" namespace="{@namespace}">.</xsl:attribute>
        </xsl:for-each>
        
        <xsl:copy-of select="."/>
        
      </xsl:element>
    </xsl:variable>
    
    <xsl:copy-of select="exslt:node-set($newRoot)/newRoot/node()"/>
    
  </xsl:template>
</xsl:stylesheet>
