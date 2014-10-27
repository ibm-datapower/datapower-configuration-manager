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
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:dp="http://www.datapower.com/extensions" 
  xmlns:exsl="http://exslt.org/common" 
  xmlns:func="http://exslt.org/functions" 
  xmlns:is="http://anything.com/is" 
  xmlns:mgmt="http://www.datapower.com/schemas/management" 
  xmlns:redirect="http://xml.apache.org/xalan/redirect" 
  xmlns:regexp="http://exslt.org/regular-expressions" 
  xmlns:str="http://exslt.org/strings" 
  xmlns:tns="http://www.datapower.com/schemas/management"
  xmlns:util="http://anything.com/util" 
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
  xmlns:xyz="urn:x:y:z" 
  extension-element-prefixes="dp redirect regexp" 
  exclude-result-prefixes="str">


  <xsl:output method="xml" indent="yes"/>


  <xsl:key name="names" match="xsd:schema/xsd:*" use="@name"/>  <!-- this key is the reason that dpschema-collectxml.xsl isn't part of this stylesheet. -->

  <xsl:param name="MARKER_BASE" select="'~Base~'"/>
  <xsl:param name="MARKER_ENUM" select="'~Enum~'"/>
  <xsl:param name="MARKER_PATTERN" select="'~Pattern~'"/>
  <xsl:param name="MARKER_TYPE" select="'~Type~'"/>
  <xsl:param name="MARKER_UNBOUNDED" select="'~Unbounded~'"/>
  <xsl:param name="SAMPLE_DOMAIN" select="'~domain~'"/>

  <xsl:variable name="wholeDoc" select="/"/>

  <!-- 
    This stylesheet accepts a WSDL and schema (produced by dpschema-collectxml.xsl) and reformats
    it into a more readable form.
  -->
  <xsl:template match="/">
    <root>
      <xsl:attribute name="xyz:dummy"/> <!-- Force xmlns:xyz to occur here so that XPaths work in my XML Editor. -->
      <xsl:variable name="request" select="util:findDefinition('request')"/>
      <xsl:for-each select="$request">
        <xsl:apply-templates select="." mode="generateRequests"/>
      </xsl:for-each>
    </root>
  </xsl:template>

  <xsl:template match="xsd:complexType | xsd:element" mode="generateRequests">
    <xsl:apply-templates select="*" mode="generateRequests"/>
  </xsl:template>

  <xsl:template match="xsd:choice" mode="generateRequests">
    <xsl:for-each select="*">
      <xsl:element name="mgmt:request">
        <xsl:attribute name="domain">
          <xsl:value-of select="$SAMPLE_DOMAIN"/>
        </xsl:attribute>
        <xsl:apply-templates select="." mode="fillBody">
          <xsl:with-param name="inMgmtNamespace" select="true()"/>
          <xsl:with-param name="emptyElementPermitted" select="false()"/>
        </xsl:apply-templates>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>



  <xsl:template match="xsd:simpleType/xsd:union" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <!-- 
      This construct was introduced in 7.0:
        <xsd:union memberTypes="tns:dmAAAPAuthenticateType tns:dmEmptyElement" />
    -->
    <xsl:variable name="type" select="substring-before(@memberTypes, ' ')"/>
    <xsl:variable name="next" select="util:findDefinition($type)"/>
    <xsl:choose>
      <xsl:when test="$next!=''">
        <!-- 
          Adding this attribute here is subtle and deserves some explanation.  The current context may
          be at a point where we can attach an attribute to the current element (e.g. AUMethod).  In that
          case this works as expected.  When the current element is something like ObjectClass then this
          does not work and we pass the flag to the next template, where it is eventually picked up by
          the next xsd:element.
        -->
        <xsl:attribute name="xyz:emptyelement">permitted</xsl:attribute>
        <!-- Follow the definition. -->
        <xsl:apply-templates select="$next/xsd:attribute | $next/xsd:attributeGroup" mode="fillBody">
          <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
          <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="$next/*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
          <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
          <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment><xsl:value-of select="concat(' ', $MARKER_BASE, ' ', $type, ' ')"/></xsl:comment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="xsd:all | xsd:complexContent | xsd:complexType | xsd:extension | xsd:group | xsd:sequence | xsd:simpleContent | xsd:simpleType" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="xsd:any" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:element name="xyz:any">
      <xsl:call-template name="util:noteOccurrences"/>
      <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="xsd:attribute" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:attribute name="{@name}">
      <xsl:value-of select="util:attrValue(@type)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="xsd:attributeGroup" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:choose>
      <xsl:when test="@ref">
        <!-- This xsd:attributeGroup references another xsd:attributeGroup. -->
        <xsl:variable name="next" select="util:findDefinition(@ref)"/>
        <xsl:choose>
          <xsl:when test="$next!=''">
            <xsl:apply-templates select="$next" mode="fillBody">
              <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
              <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">xsd:attributeGroup ref="<xsl:value-of select="@ref"/>" - referenced definition not found.</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- This xsd:attributeGroup is expected to have child xsd:attributes. -->
        <xsl:apply-templates select="*" mode="fillBody">
          <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
          <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsd:choice" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:element name="xyz:choice">

      <xsl:call-template name="util:noteOccurrences"/>
      <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>

    </xsl:element>
  </xsl:template>

  <xsl:template match="xsd:element" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:if test="@name!='filler'">
      <xsl:variable name="fullName">
        <xsl:choose>
          <xsl:when test="$inMgmtNamespace">
            <xsl:value-of select="concat('mgmt:', @name)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:element name="{$fullName}">
        <xsl:call-template name="util:noteOccurrences"/>
        <xsl:if test="$emptyElementPermitted = true()">
          <xsl:attribute name="xyz:emptyelement">permitted</xsl:attribute>
        </xsl:if>
        <xsl:choose>
          <xsl:when test="@type and count(*)=0">
            <!-- The element references a definition, so follow that pointer. -->
            <xsl:variable name="next" select="util:findDefinition(@type)"/>
            <xsl:choose>
              <xsl:when test="$next!=''">
                <xsl:apply-templates select="$next" mode="fillBody">
                  <xsl:with-param name="inMgmtNamespace" select="false()"/>
                  <xsl:with-param name="emptyElementPermitted" select="false()"/>
                </xsl:apply-templates>
              </xsl:when>
              <xsl:otherwise>
                <xsl:comment><xsl:value-of select="concat(' ', $MARKER_TYPE, ' ', @type, ' ')"/></xsl:comment>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="not(@type) and count(*)>0">
            <!-- The element has children that define it. -->
            <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
              <xsl:with-param name="inMgmtNamespace" select="false()"/>
              <xsl:with-param name="emptyElementPermitted" select="false()"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
              <xsl:with-param name="inMgmtNamespace" select="false()"/>
              <xsl:with-param name="emptyElementPermitted" select="false()"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">xsd:element found an entry that doesn't fit the expected pattern.</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xsd:extension" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <!-- Capture any attributes that are immediate children. -->
    <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>

    <xsl:if test="@base">
      <xsl:variable name="next" select="util:findDefinition(@base)"/>
      <xsl:choose>
        <xsl:when test="$next!=''">
          <!-- Follow the definition for the base class. -->
          <xsl:apply-templates select="$next/xsd:attribute | $next/xsd:attributeGroup" mode="fillBody">
            <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
            <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
          </xsl:apply-templates>
          <xsl:apply-templates select="$next/*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
            <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
            <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:comment><xsl:value-of select="concat(' ', $MARKER_BASE, ' ',@base,' ')"/></xsl:comment>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <!-- Finally capture any children elements. -->
    <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="xsd:pattern" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:comment> <xsl:value-of select="$MARKER_PATTERN"/> pattern=<xsl:value-of select="@value"/></xsl:comment>
  </xsl:template>

  <xsl:template match="xsd:restriction" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>

    <xsl:variable name="enumText">
      <xsl:for-each select="xsd:enumeration">
        <xsl:if test="position()!=1">
          <xsl:text/>, <xsl:text/>
        </xsl:if>
        <xsl:text/>
        <xsl:value-of select="@value"/>
        <xsl:text/>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="next" select="util:findDefinition(@base)"/>
    <xsl:if test="$next!=''">
      <!-- Follow the definition for the base class. -->
      <xsl:apply-templates select="$next/xsd:attribute | $next/xsd:attributeGroup" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="$next/*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup' and name()!='xsd:enumeration']" mode="fillBody">
      <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
      <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
    </xsl:apply-templates>

    <xsl:choose>
      <xsl:when test="$enumText!=''">
        <xsl:comment><xsl:value-of select="concat(' ', $MARKER_ENUM, ' ', $enumText, ' ')"/></xsl:comment>
      </xsl:when>
      <xsl:when test="$next=''">
        <xsl:comment> <xsl:value-of select="concat(' ', $MARKER_BASE, ' ', @base, ' ')"/> </xsl:comment>
      </xsl:when>
      <xsl:when test="$next!=''">
        <!-- The real work was done above, no need to emit a comment here. -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">xsd:restriction (<xsl:value-of select="@base"/>) : failed to output a comment.</xsl:message>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- xsd:unique defines a constraint that applies to another element, so we don't care about it in the dpschema. -->
  <xsl:template match="xsd:unique" mode="fillBody"/>

  <xsl:template match="xsd:*" mode="fillBody">
    <xsl:param name="inMgmtNamespace"/>
    <xsl:param name="emptyElementPermitted"/>
    <xsl:element name="xyz:unprocessed">
      <!-- Make any new xsd:* elements stand out when we fail to process them. -->
      <xsl:attribute name="name">
        <xsl:value-of select="name()"/>
      </xsl:attribute>
      <xsl:apply-templates select="xsd:attribute | xsd:attributeGroup" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="*[name()!='xsd:attribute' and name()!='xsd:attributeGroup']" mode="fillBody">
        <xsl:with-param name="inMgmtNamespace" select="$inMgmtNamespace"/>
        <xsl:with-param name="emptyElementPermitted" select="$emptyElementPermitted"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>


  <!-- 
    This function looks up the definition of the supplied name, returning a nodeset or ''
    You can test the results of the function like this:
      util:findDefinition('something')=''
    this test returns false when a definition was found, and true when no definition was found.
  -->
  <func:function name="util:findDefinition">
    <xsl:param name="definitionName"/>

    <xsl:variable name="result">
      <xsl:for-each select="$wholeDoc">
        <!-- Look first for the entire name, as supplied. -->
        <xsl:variable name="byFullName" select="key('names', $definitionName)"/>
        <xsl:choose>
          <xsl:when test="$byFullName">
            <xsl:copy-of select="exsl:node-set($byFullName)"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- That didn't work, see if the name begins with a known prefix we might remove. -->
            <xsl:choose>
              <xsl:when test="starts-with($definitionName, 'tns:')">
                <xsl:variable name="byTNS" select="key('names', substring($definitionName,5))"/>
                <xsl:choose>
                  <xsl:when test="$byTNS">
                    <xsl:copy-of select="exsl:node-set($byTNS)"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:message>Couldn't find definition of <xsl:value-of select="$definitionName"/></xsl:message>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <!-- No recognizable prefix that we can ignore, so there is nothing matching this name. -->
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>

    <func:result select="exsl:node-set($result)"/>

  </func:function>



  <!-- 
    This function takes a value for an attribute and either passes it back unchanged,
    or, finds the definition pointed to by that value and return the enumeration for 
    that value.  For example, tns:StatusEnum maps to a long string of enumerated values.
  -->
  <func:function name="util:attrValue">
    <xsl:param name="value"/>
    <func:result>
      <xsl:variable name="definition" select="util:findDefinition($value)"/>
      <xsl:choose>
        <xsl:when test="$definition!=''">
          <!-- Found the definition, so grab its guts, then return the resulting string minus the trailing comma and blank. -->
          <xsl:variable name="textWithCommas">
            <xsl:text/>
            <xsl:apply-templates select="$definition" mode="util:attrValue"/>
            <xsl:text/>
          </xsl:variable>
          <xsl:text/>
          <xsl:value-of select="substring($textWithCommas, 1, string-length($textWithCommas)-2)"/>
          <xsl:text/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Didn't find a definition, so just return the original value. -->
          <xsl:text/>
          <xsl:value-of select="$value"/>
          <xsl:text/>
        </xsl:otherwise>
      </xsl:choose>
    </func:result>

  </func:function>

  <xsl:template match="xsd:*" mode="util:attrValue">
    <xsl:apply-templates select="*" mode="util:attrValue"/>
  </xsl:template>

  <xsl:template match="xsd:enumeration" mode="util:attrValue">
    <xsl:text/><xsl:value-of select="@value"/>, <xsl:text/>
  </xsl:template>


  <!-- 
    This template considers the @minOccurs and @maxOccurs values and may add an attribute
    to the element that is currently being defined in the output tree.
  -->
  <xsl:template name="util:noteOccurrences">
    <xsl:variable name="minOccurs">
      <xsl:choose>
        <xsl:when test="@minOccurs='0' or @minOccurs='1'">
          <xsl:value-of select="@minOccurs"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'1'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="maxOccurs">
      <xsl:choose>
        <xsl:when test="@maxOccurs='1' or @maxOccurs='unbounded'">
          <xsl:value-of select="@maxOccurs"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'1'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="occurrences">
      <xsl:choose>
        <xsl:when test="$minOccurs='0' and $maxOccurs='1'">
          <xsl:value-of select="'optional: 0-1'"/>
        </xsl:when>
        <xsl:when test="$minOccurs='0' and $maxOccurs='unbounded'">
          <xsl:value-of select="'optional: 0-N'"/>
        </xsl:when>
        <xsl:when test="$minOccurs='1' and $maxOccurs='unbounded'">
          <xsl:value-of select="'required: 1-N'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'required: 1-1'"/>
          <!-- By default, each thing is "required" -->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$occurrences!=''">
      <xsl:attribute name="xyz:occurrences">
        <xsl:value-of select="$occurrences"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
