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

  This stylesheet contains a few useful namespace-oriented utility functions.

-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common"
  xmlns:func="http://exslt.org/functions"
  xmlns:local="urn:local:function"
  extension-element-prefixes="exslt" 
  exclude-result-prefixes="func exslt local">
  
  <xsl:include href="nodesetUtils.xsl"/>
  <xsl:include href="namespaceUtilsImpl.xsl"/>
  
 
  <!--
    Return an array of elements that define the namespaces and prefixes used in the nodeset.
    Relies on local:nameAndNamespace to generate the <definition> elements that are returned.
  -->
  <func:function name="local:summarizeNamespaces">
    <xsl:param name="nodeset"/>
    <xsl:param name="extraStuff" select="false()"/>
    <func:result>
      <xsl:variable name="result">

        <!-- Collect definitions of all the namespaces, which occur in any order and probably include lots of duplicates. -->
        <xsl:variable name="namespacesRaw">
          <xsl:for-each select="$nodeset//*|$nodeset//@*">
            <xsl:copy-of select="local:nameAndNamespace(., $extraStuff)"/>
          </xsl:for-each>
        </xsl:variable>
        
        <!-- Sort the definitions by namespace then prefix, and return the unique ones. -->
        <xsl:variable name="namespacesSorted">
          <xsl:for-each select="exslt:node-set($namespacesRaw)/definition">
            <xsl:sort select="@namespace"/>
            <xsl:sort select="@prefix"/>
            <xsl:copy-of select="."/>
          </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="exslt:node-set($namespacesSorted)/definition">
          <xsl:if test="(position()=1) or (@namespace!=preceding-sibling::definition[1]/@namespace or @prefix!=preceding-sibling::definition[1]/@prefix)">
            <xsl:copy-of select="."/>
          </xsl:if>
        </xsl:for-each>
        
      </xsl:variable>
      
      <xsl:copy-of select="exslt:node-set($result)"/>
      
    </func:result>
  </func:function>
  
  
  <!--
    This method returns a description of the element or attribute (it produces nothing useful
    for other types of nodes).
  -->
  <func:function name="local:nameAndNamespace">
    <xsl:param name="node"/>
    <xsl:param name="extraStuff" select="false()"/>
    <func:result>
      <xsl:for-each select="$node">
        
        <xsl:element name="definition">
          
          <xsl:attribute name="prefix">
            <xsl:if test="contains(name(), ':')">
              <xsl:value-of select="substring-before(name(), ':')"/>
            </xsl:if>
          </xsl:attribute>
          
          <xsl:attribute name="namespace">
            <xsl:value-of select="namespace-uri()"/>
          </xsl:attribute>
          
          <xsl:if test="$extraStuff">

            <xsl:attribute name="name">
              <xsl:value-of select="name()"/>
            </xsl:attribute>
            
            <xsl:attribute name="local-name">
              <xsl:value-of select="local-name()"/>
            </xsl:attribute>
            
            <xsl:attribute name="type">
              <xsl:value-of select="local:determineNodeType(.)"/>
            </xsl:attribute>
            
          </xsl:if>
        </xsl:element>
        
      </xsl:for-each>
    </func:result>
  </func:function>
  
  
  <!--
    Filter a list of <definition>s (as returned by local:summarizeNamespaces(), for example) to show only
    those for prefixes that are associated with more than one namespace.
  -->
  <func:function name="local:detectRedefinedPrefixes">
    <xsl:param name="definitions"/>
    <func:result>
      <!--
        The input is an array of unique <definition>s in sorted order based on namespace then prefix.
        Peel out a list of unique prefixes.
      -->
      
      <xsl:variable name="prefixesRaw">
        <xsl:for-each select="$definitions/definition">
          <xsl:sort select="@prefix"/>
          <xsl:copy-of select="."/>
        </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="prefixesUnique">
        <xsl:for-each select="exslt:node-set($prefixesRaw)/definition">
          <xsl:if test="(position()=1) or (@prefix!=preceding-sibling::definition[1]/@prefix)">
            <xsl:copy-of select="."/>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <!-- Scan the original definitions looking for redefinitions. -->
      <xsl:for-each select="exslt:node-set($prefixesUnique)/definition">
        <xsl:variable name="prefix" select="@prefix"/>
        <xsl:if test="count($definitions/definition[@prefix=$prefix]) &gt; 1">
          <xsl:copy-of select="$definitions/definition[@prefix=$prefix]"/>
        </xsl:if>
      </xsl:for-each>
      
    </func:result>
  </func:function>
  
  
  <!--
    Filter a list of <definition>s (as returned by local:summarizeNamespaces(), for example) to show only
    those for namespaces that are associated with more than one prefix.
  -->
  <func:function name="local:detectRedefinedNamespaces">
    <xsl:param name="definitions"/>
    <func:result>
      <!--
        The input is an array of unique <definition>s in sorted order based on namespace then prefix.
        Peel out a list of unique namespaces.
      -->
      <xsl:variable name="namespacesUnique">
        <xsl:for-each select="$definitions/definition">
          <xsl:if test="(position()=1) or (@namespace!=preceding-sibling::definition[1]/@namespace)">
            <xsl:copy-of select="."/>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <!-- Scan the original definitions looking for redefinitions. -->
      <xsl:for-each select="exslt:node-set($namespacesUnique)/definition">
        <xsl:variable name="namespace" select="@namespace"/>
        <xsl:if test="count($definitions/definition[@namespace=$namespace]) &gt; 1">
          <xsl:copy-of select="$definitions/definition[@namespace=$namespace]"/>
        </xsl:if>
      </xsl:for-each>
      
    </func:result>
  </func:function>
  
  
  <!--
    Filter a list of <definition>s (as returned by local:summarizeNamespaces(), for example) to show only
    those for prefixes that are defined once.
  -->
  <func:function name="local:detectUniquePrefixes">
    <xsl:param name="definitions"/>
    <func:result>
      <!--
        The input is an array of unique <definition>s in sorted order based on namespace then prefix.
        Peel out a list of unique prefixes.
      -->
      <xsl:variable name="prefixesRaw">
        <xsl:for-each select="exslt:node-set($definitions)/definition">
          <xsl:sort select="@prefix"/>
          <xsl:copy-of select="."/>
        </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="prefixesUnique">
        <xsl:for-each select="exslt:node-set($prefixesRaw)/definition">
          <xsl:if test="(position()=1) or (@prefix!=preceding-sibling::definition[1]/@prefix)">
            <xsl:copy-of select="."/>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <!-- Scan the original definitions looking for singleton prefix definitions. -->
      <xsl:for-each select="exslt:node-set($prefixesUnique)/definition">
        <xsl:variable name="prefix" select="@prefix"/>
        <xsl:if test="count($definitions/definition[@prefix=$prefix]) = 1">
          <xsl:copy-of select="$definitions/definition[@prefix=$prefix]"/>
        </xsl:if>
      </xsl:for-each>
      
    </func:result>
  </func:function>
  
  
  <!--
    Hoist one or more prefix:namespace definitions to the root element in a document.
    
    The $rootElem must be a proper document, with a document node and a single root
    element.
    
    The $definitions must be one or more <definition> elements, in the same form
    produced by local:nameAndNamespace().
    
    The returned document is identical to the original document, except that the
    specified prefixes are defined in the root element, and interior elements shouldn't
    generally have the specified prexies defined locally.
  -->
  <func:function name="local:hoistPrefixDefinitions">
    <xsl:param name="rootElem"/>
    <xsl:param name="definitions"/>
    <func:result>
      
      <xsl:apply-templates select="$rootElem" mode="hoistPrefixDefinitions">
        <xsl:with-param name="definitions" select="$definitions"/>
      </xsl:apply-templates>
      
    </func:result>
  </func:function>
  
  
  
  <!--
    Remove excess namespace definitions.
  -->
  <func:function name="local:cleanupNamespaceDefinitions">
    <xsl:param name="nodeset"/>
    <func:result>
      <xsl:variable name="result">
        <xsl:apply-templates select="$nodeset" mode="local:cleanupNamespaceDefinitions"/>
      </xsl:variable>
      <xsl:copy-of select="$result"/>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:cleanupNamespaceDefinitions">
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="local:cleanupNamespaceDefinitions"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*" mode="local:cleanupNamespaceDefinitions">
    <xsl:choose>
      <xsl:when test="namespace-uri()!=''">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
          <xsl:apply-templates select="@*" mode="local:cleanupNamespaceDefinitions"/>
          <xsl:apply-templates select="node()" mode="local:cleanupNamespaceDefinitions"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="{name()}">
          <xsl:apply-templates select="@*" mode="local:cleanupNamespaceDefinitions"/>
          <xsl:apply-templates select="node()" mode="local:cleanupNamespaceDefinitions"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="@*" mode="local:cleanupNamespaceDefinitions">
    <xsl:choose>
      <xsl:when test="namespace-uri()!=''">
        <xsl:attribute name="{name()}" namespace="{namespace-uri()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="text()|comment()|processing-instruction()" mode="local:cleanupNamespaceDefinitions">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  
  
  <!--
    Examine the document and hoist all the unique prefix/namespace definitions to the top.
    
    This fits two use cases:
    
    1. A non-standard XML parser, for example an ad-hoc parser written in Java, can't deal
       with namespaces occuring willy nilly in the document.
    2. The namespaces make the XML hard to read while you are debugging.
    
    It is potentially a good deal of processing for a cosmetic effect, so I encourage you to
    use it with some discretion.
  -->
  <func:function name="local:hoistNamespaceDefinitions">
    <xsl:param name="nodeset"/>
    <func:result>
      <xsl:variable name="cleanedup" select="local:cleanupNamespaceDefinitions(exslt:node-set($nodeset))"/>
      <xsl:variable name="summary" select="local:summarizeNamespaces(exslt:node-set($cleanedup))"/>
      <xsl:variable name="definitions" select="local:detectUniquePrefixes(exslt:node-set($summary))"/>
      <xsl:copy-of select="local:hoistPrefixDefinitions(exslt:node-set($cleanedup), exslt:node-set($definitions))"/>
    </func:result>
  </func:function>
  
  
  
</xsl:stylesheet>
