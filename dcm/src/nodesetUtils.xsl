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

  This stylesheet contains a few useful nodeset-oriented utility functions.

-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:func="http://exslt.org/functions"
  xmlns:local="urn:local:function"
  exclude-result-prefixes="func local">
  
  
  <xsl:variable name="quote">"</xsl:variable>
  
  
  <!--
    Determine whether the supplied node is a document node, or some other sort of node.
    If you supply a nodeset, then the first node in the nodeset is evaluated.
    Returns boolean true or false.
  -->
  <func:function name="local:isDocumentNode">
    <xsl:param name="node"/>
    <xsl:choose>
      <xsl:when test="$node">
        <xsl:variable name="tmp">
          <xsl:apply-templates select="$node" mode="local:isDocumentNode"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$tmp='true'">
            <func:result select="true()"/>
          </xsl:when>
          <xsl:otherwise>
            <func:result select="false()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <func:result select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </func:function>
  
  <xsl:template match="/" mode="local:isDocumentNode">
    <xsl:value-of select="'true'" />
  </xsl:template>
  
  <xsl:template match="node()" mode="local:isDocumentNode">
    <xsl:value-of select="'false'"/>
  </xsl:template>
  
  
  <!--
    Return a nodeset guaranteed not to have a document node.
  -->
  <func:function name="local:ensureNoDocumentNode">
    <xsl:param name="nodeset"/>
    <func:result>
      <xsl:apply-templates select="$nodeset" mode="local:ensureNoDocumentNode"/>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:ensureNoDocumentNode">
    <xsl:apply-templates select="node()" mode="local:ensureNoDocumentNode"/>
  </xsl:template> 
  
  <xsl:template match="node()" mode="local:ensureNoDocumentNode">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="local:ensureNoDocumentNode"/>
    </xsl:copy>
  </xsl:template>
  
  
  <!--
    Return a nodeset guaranteed to have a document node.
  -->
  <func:function name="local:ensureDocumentNode">
    <xsl:param name="nodeset"/>
    <func:result>
      <xsl:copy-of select="$nodeset"/>
    </func:result>
  </func:function>
  
  
  
  <!--
    Return the path up to and including the supplied element.  It is an absolute path if $el has
    a document node, otherwise it is a relative path.
  -->
  <func:function name="local:pathOfElement">
    <xsl:param name="el"/>
    <func:result>

      <xsl:variable name="partialPath">
        <xsl:for-each select="$el[1]/ancestor-or-self::*">
          <xsl:choose>
            <xsl:when test="@name!=''">
              <xsl:value-of select='concat("/", name(), "[name=&apos;", @name, "&apos;]")'/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='concat("/", name())'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      
      <xsl:choose>
        <xsl:when test="local:isDocumentNode($el)">
          <xsl:value-of select="$partialPath"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring($partialPath, 2)"/>
        </xsl:otherwise>
      </xsl:choose>
      
    </func:result>
  </func:function>
  
  
  <!--
    Return a copy, of limited depth, of the supplied node or result tree fragment.  
    
    depth=0 - return nothing
    depth=1 - root node(s) only
    depth=2 - root node(s) plus any child nodes
    ...
    
    An element includes its attributes, these are not considered another level.
  -->
  <func:function name="local:shallowCopy">
    <xsl:param name="el"/>
    <xsl:param name="depth"/>
    <func:result>
      <xsl:apply-templates select="$el" mode="local:shallowCopy">
        <xsl:with-param name="depth" select="$depth"/>
      </xsl:apply-templates>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:shallowCopy">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy>
        <xsl:apply-templates select="node()" mode="local:shallowCopy">
          <xsl:with-param name="depth" select="$depth"/>
        </xsl:apply-templates>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="local:shallowCopy">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy>
        <xsl:apply-templates select="@*" mode="local:shallowCopy">
          <xsl:with-param name="depth" select="$depth"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="local:shallowCopy">
          <xsl:with-param name="depth" select="$depth - 1"/>
        </xsl:apply-templates>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="@*" mode="local:shallowCopy">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy>
        <xsl:apply-templates select="text()" mode="local:shallowCopy">
          <xsl:with-param name="depth" select="$depth"/>
        </xsl:apply-templates>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()|comment()|processing-instruction()" mode="local:shallowCopy">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy/>
    </xsl:if>
  </xsl:template>
  
  
  <!--
    Dump the supplied node or result tree fragment in a manner that clearly reveals its structure.   
  -->
  <func:function name="local:dumpNodeset">
    <xsl:param name="el"/>
    <xsl:param name="depth" select="1000"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="$el">
          <xsl:apply-templates select="$el" mode="local:dumpNodeset">
            <xsl:with-param name="depth" select="$depth"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'&lt;null/&gt;'"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="'&lt;document&gt;'"/>
      <xsl:apply-templates select="node()" mode="local:dumpNodeset">
        <xsl:with-param name="depth" select="$depth"/>
      </xsl:apply-templates>
      <xsl:value-of select="'&lt;/document&gt;'"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="'&lt;element&gt;'"/>
      <xsl:value-of select="concat('&lt;attribute name=', $quote, 'name', $quote, '&gt;', name(), '&lt;/attribute&gt;')"/>
      <xsl:apply-templates select="@*" mode="local:dumpNodeset">
        <xsl:with-param name="depth" select="$depth"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="local:dumpNodeset">
        <xsl:with-param name="depth" select="$depth - 1"/>
      </xsl:apply-templates>
      <xsl:value-of select="'&lt;/element&gt;'"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="@*" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat('&lt;attribute name=', $quote, name(), $quote, '&gt;', string(), '&lt;/attribute&gt;')"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat('&lt;text&gt;', string(), '&lt;/text&gt;')"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="comment()" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat('&lt;comment&gt;', string(), '&lt;/comment&gt;')"/>
      <xsl:copy/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="processing-instruction()" mode="local:dumpNodeset">
    <xsl:param name="depth"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="concat('&lt;processing-instruction&gt;', string(), '&lt;/processing-instruction&gt;')"/>
      <xsl:copy/>
    </xsl:if>
  </xsl:template>


  <!--
    Serialize the supplied node or result tree fragment.   
  -->
  <func:function name="local:serializeNodeset">
    <xsl:param name="el"/>
    <xsl:param name="depth" select="1000"/>
    <xsl:param name="showDocNode" select="false()"/>
    <func:result>
      <xsl:choose>
        <xsl:when test="$el">
          <xsl:apply-templates select="$el" mode="local:serializeNodeset">
            <xsl:with-param name="depth" select="$depth"/>
            <xsl:with-param name="showDocNode" select="$showDocNode"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'&lt;null/&gt;'"/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:serializeNodeset">
    <xsl:param name="depth"/>
    <xsl:param name="showDocNode"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:if test="$showDocNode">
        <xsl:value-of select="'&lt;document&gt;'"/>
      </xsl:if>
      <xsl:apply-templates select="node()" mode="local:serializeNodeset">
        <xsl:with-param name="depth" select="$depth"/>
        <xsl:with-param name="showDocNode" select="$showDocNode"/>
      </xsl:apply-templates>
      <xsl:if test="$showDocNode">
        <xsl:value-of select="'&lt;/document&gt;'"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="local:serializeNodeset">
    <xsl:param name="depth"/>
    <xsl:param name="showDocNode"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:choose>
        <xsl:when test="node() and $depth &gt; 1">
          
          <!-- Emit an element that contains something. (<el ...>...</el>) -->
          <xsl:value-of select="concat('&lt;', name())"/>
          <xsl:for-each select="@*">
            <xsl:value-of select="concat(' ', name(), '=', $quote, string(), $quote)"/>
          </xsl:for-each>
          <xsl:value-of select="'&gt;'"/>
          <xsl:apply-templates select="node()" mode="local:serializeNodeset">
            <xsl:with-param name="depth" select="$depth - 1"/>
            <xsl:with-param name="showDocNode" select="$showDocNode"/>
          </xsl:apply-templates>
          <xsl:value-of select="concat('&lt;/', name(), '&gt;')"/>
          
        </xsl:when>
        <xsl:when test="node() and $depth = 1">
          
          <!-- Emit an element that contains something. (<el ...>...</el>) -->
          <xsl:value-of select="concat('&lt;', name())"/>
          <xsl:for-each select="@*">
            <xsl:value-of select="concat(' ', name(), '=', $quote, string(), $quote)"/>
          </xsl:for-each>
          <xsl:value-of select="concat('&gt;...&lt;', name(), '&gt;')"/>
          
        </xsl:when>
        <xsl:otherwise>
          
          <!-- Emit an element that contains nothing. (<el .../>) -->
          <xsl:value-of select="concat('&lt;', name())"/>
          <xsl:for-each select="@*">
            <xsl:value-of select="concat(' ', name(), '=', $quote, string(), $quote)"/>
          </xsl:for-each>
          <xsl:value-of select="'/&gt;'"/>
          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="local:serializeNodeset">
    <xsl:param name="depth"/>
    <xsl:param name="showDocNode"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:value-of select="."/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="comment() | processing-instruction()" mode="local:serializeNodeset">
    <xsl:param name="depth"/>
    <xsl:param name="showDocNode"/>
    <xsl:if test="$depth &gt; 0">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>
  
  
  <!--
    Return the type of a node:
    
    'document' - document root node
    'element' - element node
    'attribute' - attribute node
    'text' - text node
    'comment' - comment node
    'processing-instruction' - processing instruction
  -->
  <func:function name="local:determineNodeType">
    <xsl:param name="node"/>
    <func:result>
      <xsl:apply-templates select="$node" mode="local:determineNodeType"/>
    </func:result>
  </func:function>
  
  <xsl:template match="/" mode="local:determineNodeType">
    <xsl:value-of select="'document'"/>
  </xsl:template>
  
  <xsl:template match="*" mode="local:determineNodeType">
    <xsl:value-of select="'element'"/>
  </xsl:template>
  
  <xsl:template match="@*" mode="local:determineNodeType">
    <xsl:value-of select="'attribute'"/>
  </xsl:template>
  
  <xsl:template match="text()" mode="local:determineNodeType">
    <xsl:value-of select="'text'"/>
  </xsl:template>
  
  <xsl:template match="comment()" mode="local:determineNodeType">
    <xsl:value-of select="'comment'"/>
  </xsl:template>
  
  <xsl:template match="processing-instruction()" mode="local:determineNodeType">
    <xsl:value-of select="'processing-instruction'"/>
  </xsl:template>
  
  
</xsl:stylesheet>
