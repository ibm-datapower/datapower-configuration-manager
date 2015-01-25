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

  This stylesheet processes the contents of a dcm:object-create or dcm:object-modify,
  assimilating the contents (e.g. MultiProtocolGateway or SSLProfile) along with 
  dcm:vector-delete and dcm:vector-insert elements.
  
  This stylesheet implements one of the most complex computations that is done within DCM.
  The input is a set of object definitions fit for <set-config>, a dcm:object-create or
  dcm:object-modify, and a schema.  The result is a set of schema-compliant object definitions 
  that can be submitted in a <set-config> operation.  Essentially, we recursively walk 
  through the schema for the various object definitions (e.g. AAAPolicy, StylePolicyAction) 
  and construct brand new object definitions, that fit the schema, based on the supplied 
  object definition and the the contents of the dcm:object-modify or dcm:object-create 
  element.
  
-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
  xmlns:dcm="urn:datapower:configuration:manager"
  xmlns:cpgm="urn:configuration:manufacturing:control-program:v1" 
  xmlns:date="http://exslt.org/dates-and-times" 
  xmlns:dp="http://www.datapower.com/extensions" 
  xmlns:dyn="http://exslt.org/dynamic" 
  xmlns:es="urn:local-function:expand-soma" 
  xmlns:exslt="http://exslt.org/common" 
  xmlns:func="http://exslt.org/functions" 
  xmlns:local="urn:local:function" 
  xmlns:mgmt="http://www.datapower.com/schemas/management" 
  xmlns:om="urn:object:modify" 
  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" 
  xmlns:str="http://exslt.org/strings" 
  xmlns:xyz="urn:x:y:z" 
  extension-element-prefixes="date dyn exslt func str" 
  exclude-result-prefixes="date dyn exslt func str">

  <!-- 
    The dcm:object-modify or dcm:object-create is passed in through a file, whose 
    name is specified by this stylesheet parameter.
  -->
  <xsl:param name="input-url"/>
  <xsl:variable name="fullDcmObjectCM" select="document($input-url)"/>

  <!-- 
    The xml-mgmt*.xsd files are combined and reformatted into a more congenial form which is
    referred to here as a schema, but it isn't the standard-compliant schema that you might
    expect.  Take a look at it to get a feel for it.
  -->
  <xsl:param name="schema-url"/>
  <xsl:variable name="fullSchema" select="document($schema-url)"/>
  
  <!-- 
    Optional new name to apply to the (first) object definition.
  -->
  <xsl:param name="new-name"/>
  


  <xsl:param name="debug" select="false()"/> <!-- controls debug printing statements. -->
  <xsl:param name="debugXML" select="false()"/> <!-- controls debug info in resulting XML -->


  <xsl:template match="/">
<!--    
    <xsl:message>   @@@ input-url=<xsl:value-of select="$input-url"/></xsl:message>
    <xsl:message>   @@@ new-name=<xsl:value-of select="$new-name"/></xsl:message>
    <xsl:message>   @@@ input= <xsl:copy-of select="local:dumpNodesetAsText(.)"/></xsl:message>
-->    
    <xsl:copy> <!-- Copy the document node that owns the root element node. -->
      <xsl:apply-templates select="node()"/> <!-- Process the root element node. -->
    </xsl:copy>
  </xsl:template>


  <xsl:template match="/objs/* | /dcm:object-create/* | /*[name() != 'objs' and not(local-name() = 'object-create' and namespace-uri() = 'urn:datapower:configuration:manager')]">

    <!-- 
      This template begins the process of rewriting each object element (e.g. <AAAPolicy>, <StylePolicy>)
      present in the set-config request.  The process is recursive in order to handle things like 
      AAAPolicy/ExtractIdentity/EIBitmap.
      
      The recursive process takes each original object element in the set-config (the current
      context right now), the matching element in the schema (e.g. AAAPolicy here is matched by
      /root/dp:request/modify-config/xyz:choice/AAAPolicy in the schema file), and the set of
      changes specified as content in the dcm:object-modify (or dcm:object-create).  The
      mo:rewriteObject template recursively rewrites the object element based on these three
      inputs.
      
      Search the schema for the part that describes this type of object, and error check the result
      of that search.  When things are cool then kick off the recursion.
    -->
    <xsl:variable name="classname" select="name()"/>
    <xsl:variable name="schemaForObject" select="$fullSchema/root/mgmt:request/mgmt:set-config/xyz:choice/*[name() = $classname]"/>
    <xsl:choose>
      <xsl:when test="count($schemaForObject) = 0">
        <xsl:message terminate="yes">Attempting to set-config for a type of object (<xsl:value-of select="$classname"/>) that is not listed in the schema!</xsl:message>
      </xsl:when>
      <xsl:when test="count($schemaForObject) &gt; 1">
        <xsl:message terminate="yes">Attempting to set-config for a type of object (<xsl:value-of select="$classname"/>) and found it listed more than once in the schema!</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        
        <xsl:choose>
          <xsl:when test="$new-name">
            
            <xsl:variable name="renamed">
              <xsl:element name="{$classname}">
                <xsl:attribute name="name"><xsl:value-of select="$new-name"/></xsl:attribute>
                <xsl:copy-of select="@*[name() != 'name'] | node()"/>
              </xsl:element>
            </xsl:variable>
            
            <xsl:copy-of select="om:rewriteAccordingToSchema($schemaForObject, exslt:node-set($renamed)/*, $fullDcmObjectCM, '')"/>
            
          </xsl:when>
          <xsl:otherwise>
            
            <xsl:copy-of select="om:rewriteAccordingToSchema($schemaForObject, ., $fullDcmObjectCM, '')"/>
            
          </xsl:otherwise>
        </xsl:choose>
        
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- 
    Recursively write an object definition according to this schema node (and any children it may have).
  -->
  <func:function name="om:rewriteAccordingToSchema">
    <xsl:param name="schemaNode"/>
    <xsl:param name="rawInput"/>
    <xsl:param name="dcmObjectCM"/> <!-- dcm:object-create or dcm:object-modify element -->
    <xsl:param name="pathParent"/>   <!-- relative xpath to parent (e.g. AAAPolicy/ExtractIdentity/EIBitmap/) -->
    <func:result>

      <xsl:variable name="prefix" select="concat('rewrite (', $pathParent, ') : ')"/>

      <xsl:variable name="name" select="name($schemaNode)"/>
      <xsl:choose>
        <xsl:when test="$name = 'xyz:any'">

          <xsl:message terminate="yes"><xsl:value-of select="$prefix"/>Encountered xyz:any in schema, which we don't know how to deal with at the moment.</xsl:message>

        </xsl:when>
        <xsl:when test="$name = 'xyz:choice'">

          <xsl:if test="$debug">
            <xsl:message><xsl:value-of select="$prefix"/>Encountered xyz:choice in schema, recursing.</xsl:message>
          </xsl:if>

          <!-- Recurse for each possible choice. -->
          <xsl:variable name="kids">
            <xsl:for-each select="$schemaNode/*">
              <xsl:copy-of select="om:rewriteAccordingToSchema(., $rawInput, $dcmObjectCM, $pathParent)"/>
            </xsl:for-each>
          </xsl:variable>

          <!-- Ensure that the choice generated something. -->
          <xsl:if test="not($kids)">
            <xsl:message terminate="yes"><xsl:value-of select="$prefix"/>Encountered xyz:choice, which requires something to be output, but nothing was generated based on the input!.</xsl:message>
          </xsl:if>

          <xsl:if test="$debug">
            <xsl:message><xsl:value-of select="$prefix"/>Encountered xyz:choice in schema, finished recursing.</xsl:message>
          </xsl:if>

          <xsl:copy-of select="$kids"/>

        </xsl:when>
        <xsl:otherwise>

          <!-- 
            Found a plain old element (e.g. <mAdminState>) in the schema.  Emit this element 
            when the schema requires it, when the raw input specifies it, or when the changes 
            specify it.
          -->

          <!-- This element, or its parent or grandparent, will have an xyz:occurrences attribute. -->
          <xsl:variable name="occurrences" select="concat($schemaNode/@xyz:occurrences, $schemaNode/../@xyz:occurrences, $schemaNode/../../@xyz:occurrences)"/>

          <!-- Notice whether the schema requires this element. -->
          <xsl:variable name="required">
            <xsl:choose>
              <xsl:when test="starts-with($occurrences, 'required:')">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <!-- Notice whether the incoming raw object definition contains this element. -->
          <xsl:variable name="inRaw">
            <xsl:choose>
              <xsl:when test="exslt:node-set($rawInput)[name() = $name]">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <!-- Notice whether the changes contain this element. -->
          <xsl:variable name="parentname" select="name($schemaNode/ancestor::*[namespace-uri()!='urn:x:y:z'][1])"/>
          <xsl:variable name="inChanges">
            <xsl:choose>
              <xsl:when test="$dcmObjectCM/*/*[name() = $name and (not(@dcm:parentclass) or @dcm:parentclass = $parentname)]">
                <xsl:value-of select="'true'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'false'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <xsl:if test="$debug">
            <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema : required=<xsl:value-of select="$required"/>, inRaw=<xsl:value-of select="$inRaw"/>, inChanges=<xsl:value-of select="$inChanges"/>.</xsl:message>
          </xsl:if>

          <!-- Produce the final collection of this type of element. -->
          <xsl:variable name="collectedElements">

            <!-- Notice a dcm:vector-insert-before that is unqualified (its contents should precede all these elements from the input). -->
            <xsl:for-each select="$dcmObjectCM/*/dcm:vector-insert-before[@class = $name and count(@*) = 1]/*">
              <xsl:call-template name="handleOneElement">
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                <xsl:with-param name="schemaNode" select="$schemaNode"/>
                <xsl:with-param name="pathParent" select="$pathParent"/>
                <xsl:with-param name="marker" select="'insert-vector-before-global'"/>
              </xsl:call-template>
            </xsl:for-each>

            <!-- Notice the presence of a dcm:vector-delete or a dcm:vector-replace that is unqualified - this prevents any of this kind of element from being copied from the input. -->
            <xsl:variable name="deleteAll" select="$dcmObjectCM/*/dcm:vector-delete[@class = $name and count(@*) = 1] or $dcmObjectCM/*/dcm:vector-replace[@class = $name and count(@*) = 1]"/>

            <xsl:if test="$required = 'true' or $inRaw = 'true' or $inChanges = 'true'">

              <xsl:choose>
                <xsl:when test="$inChanges = 'true'">

                  <!-- Emit this element supplied in the changes (dcm:object-create or dcm:object-modify), then recurse. -->
                  <xsl:variable name="elChange" select="$dcmObjectCM/*/*[name() = $name and (not(@dcm:parentclass) or @dcm:parentclass = $parentname)]"/>

                  <xsl:if test="$debug">
                    <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema - copying that node from the dcm:object-*.</xsl:message>
                  </xsl:if>

                  <!-- Process the found element(s). -->
                  <xsl:for-each select="$elChange">

                    <xsl:call-template name="handleOneElement">
                      <xsl:with-param name="name" select="$name"/>
                      <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                      <xsl:with-param name="schemaNode" select="$schemaNode"/>
                      <xsl:with-param name="pathParent" select="$pathParent"/>
                      <xsl:with-param name="marker" select="'changes'"/>
                    </xsl:call-template>

                  </xsl:for-each>

                  <!-- Emit this element supplied in the dcm:object-create or dcm:object-modify, then recurse. -->
                  <xsl:if test="$debug">
                    <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema - finished copying that node from the dcm:object-create or dcm:object-modify.</xsl:message>
                  </xsl:if>

                </xsl:when>
                <xsl:when test="$inRaw = 'true'">

                  <xsl:variable name="elRaw" select="$rawInput[name() = $name]"/>
                  <!-- Pick up the elements from the input for this name (e.g. StylePolicy). -->

                  <xsl:if test="$debug">
                    <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema - copying that node from the input.</xsl:message>
                  </xsl:if>

                  <!-- Process the found element(s) from the input. -->
                  <xsl:for-each select="$elRaw">

                    <xsl:variable name="thisRaw" select="."/>

                    <!-- Insert any elements that should precede this one. -->
                    <xsl:for-each select="$dcmObjectCM/*/dcm:vector-insert-before[om:isQualified($thisRaw, .)]/*">
                      <xsl:call-template name="handleOneElement">
                        <xsl:with-param name="name" select="$name"/>
                        <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                        <xsl:with-param name="schemaNode" select="$schemaNode"/>
                        <xsl:with-param name="pathParent" select="$pathParent"/>
                        <xsl:with-param name="marker" select="'insert-vector-before'"/>
                      </xsl:call-template>
                    </xsl:for-each>

                    <!-- Either copy this element, or replace it. -->
                    <xsl:choose>
                      <xsl:when test="$dcmObjectCM/*/dcm:vector-replace[om:isQualified($thisRaw, .)]">

                        <!-- Use the contents of the applicable dcm:vector-replace elements. -->
                        <xsl:for-each select="$dcmObjectCM/*/dcm:vector-replace[om:isQualified($thisRaw, .)]/*">
                          <xsl:call-template name="handleOneElement">
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                            <xsl:with-param name="schemaNode" select="$schemaNode"/>
                            <xsl:with-param name="pathParent" select="$pathParent"/>
                            <xsl:with-param name="marker" select="'insert-vector-replace'"/>
                          </xsl:call-template>
                        </xsl:for-each>

                      </xsl:when>
                      <xsl:otherwise>

                        <!-- Copy the content of this element, provided it isn't deleted. -->
                        <xsl:if test="not($deleteAll) and not($dcmObjectCM/*/dcm:vector-delete[om:isQualified($thisRaw, .)])">

                          <xsl:call-template name="handleOneElement">
                            <xsl:with-param name="name" select="$name"/>
                            <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                            <xsl:with-param name="schemaNode" select="$schemaNode"/>
                            <xsl:with-param name="pathParent" select="$pathParent"/>
                            <xsl:with-param name="marker" select="'plain-copy'"/>
                          </xsl:call-template>

                        </xsl:if>

                      </xsl:otherwise>
                    </xsl:choose>

                    <!-- Insert any elements that should follow this one. -->
                    <xsl:for-each select="$dcmObjectCM/*/dcm:vector-insert-after[om:isQualified($thisRaw, .)]/*">
                      <xsl:call-template name="handleOneElement">
                        <xsl:with-param name="name" select="$name"/>
                        <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                        <xsl:with-param name="schemaNode" select="$schemaNode"/>
                        <xsl:with-param name="pathParent" select="$pathParent"/>
                        <xsl:with-param name="marker" select="'insert-vector-after'"/>
                      </xsl:call-template>
                    </xsl:for-each>

                  </xsl:for-each>

                  <xsl:if test="$debug">
                    <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema - finished copying that node from the input.</xsl:message>
                  </xsl:if>

                </xsl:when>
                <xsl:when test="$required = 'true'">

                  <!-- 
                    This element is required but not present in the raw object definition or in the changes.
                    Emit this (and any required children) based on the schema itself.
                  -->
                  <xsl:element name="{$name}">
                    <xsl:call-template name="rewriteAttributes">
                      <xsl:with-param name="schemaNode" select="$schemaNode"/>
                      <xsl:with-param name="inputNode" select="/.."/>
                    </xsl:call-template>
                    <xsl:for-each select="$schemaNode/*">
                      <xsl:copy-of select="om:rewriteAccordingToSchema(., /.., $dcmObjectCM, concat($pathParent, $name, '/'))"/>
                    </xsl:for-each>
                  </xsl:element>

                </xsl:when>

              </xsl:choose>

            </xsl:if>

            <!-- Notice a dcm:vector-insert-after or dcm:vector-replace that is unqualified (its contents should follow all these elements from the input). -->
            <xsl:for-each select="$dcmObjectCM/*/dcm:vector-insert-after[@class = $name and count(@*) = 1]/* | $dcmObjectCM/*/dcm:vector-replace[@class = $name and count(@*) = 1]/*">
              <xsl:call-template name="handleOneElement">
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="dcmObjectCM" select="$dcmObjectCM"/>
                <xsl:with-param name="schemaNode" select="$schemaNode"/>
                <xsl:with-param name="pathParent" select="$pathParent"/>
                <xsl:with-param name="marker" select="'insert-vector-after-global'"/>
              </xsl:call-template>
            </xsl:for-each>

          </xsl:variable>

          <!-- Check whether an excessive number of this element have been supplied. -->
          <xsl:if test="contains($schemaNode/@xyz:occurrences, '-1') and count(exslt:node-set($collectedElements)) &gt; 1">
            <xsl:message terminate="yes">Found <xsl:value-of select="count(exslt:node-set($collectedElements))"/> copies of <xsl:value-of select="$name"/> in the dcm:object-create or dcm:object-modify, but the schema permits only one.</xsl:message>
          </xsl:if>

          <!-- Check whether this element was required but isn't in the collected output. -->
          <xsl:if test="starts-with($schemaNode/@xyz:occurrences, 'required') and count(exslt:node-set($collectedElements)) = 0">
            <xsl:message terminate="yes">Found no copies of <xsl:value-of select="$name"/> in the dcm:object-create or dcm:object-modify, but the schema requires one.</xsl:message>
          </xsl:if>

          <xsl:if test="$debug">
            <xsl:message><xsl:value-of select="$prefix"/>Encountered <xsl:value-of select="$name"/> in schema - finished.</xsl:message>
          </xsl:if>

          <xsl:copy-of select="exslt:node-set($collectedElements)"/>

        </xsl:otherwise>
      </xsl:choose>
    </func:result>
  </func:function>


  <xsl:template name="rewriteAttributes">
    <xsl:param name="schemaNode"/>
    <xsl:param name="inputNode"/>

    <xsl:if test="$debug">
      <xsl:message> copying attributes for a <xsl:value-of select="name($schemaNode)"/></xsl:message>
    </xsl:if>
    
    <xsl:for-each select="$schemaNode/@*">
      <xsl:variable name="attrname" select="name()"/>
      <xsl:choose>
        <xsl:when test="$inputNode/@*[name() = $attrname]">

          <xsl:attribute name="{$attrname}">
            <xsl:value-of select="$inputNode/@*[name() = $attrname]"/>
          </xsl:attribute>

          <xsl:if test="$debug">
            <xsl:message>  copied from input or changes node <xsl:value-of select="concat($attrname, '=', $inputNode/@*[name() = $attrname])"/> from <xsl:value-of select="name($inputNode)"/></xsl:message>
          </xsl:if>

        </xsl:when>
        <xsl:otherwise>

          <xsl:if test="namespace-uri(.) = ''">

            <xsl:variable name="value">
              <xsl:if test="starts-with(., 'xsd:') != true()">
                <xsl:value-of select="."/>
              </xsl:if>
            </xsl:variable>
            <xsl:if test="$value != ''">

              <xsl:attribute name="{$attrname}">
                <xsl:value-of select="$value"/>
              </xsl:attribute>
              <xsl:if test="$debug">
                <xsl:message>  copied from schema node <xsl:value-of select="concat($attrname, '=', $value)"/> from <xsl:value-of select="name($schemaNode)"/></xsl:message>
              </xsl:if>

            </xsl:if>

          </xsl:if>

        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>

  </xsl:template>





  <xsl:template name="rewriteText">
    <xsl:param name="schemaNode"/>
    <xsl:param name="inputNode"/>

    <xsl:choose>
      <xsl:when test="$inputNode/text()">

        <xsl:if test="$debug">
          <xsl:message> copying text for a <xsl:value-of select="name($inputNode)"/> : input or dcm:object-* : <xsl:value-of select="$inputNode/text()"/></xsl:message>
        </xsl:if>

        <!-- Copy the text from the input. -->
        <xsl:value-of select="$inputNode/text()"/>

      </xsl:when>
      <xsl:otherwise>

        <!-- No text in the input or changes, so see if the schema has any. -->
        <xsl:if test="normalize-space($schemaNode/text()) != ''">

          <xsl:if test="$debug">
            <xsl:message> copying text for a <xsl:value-of select="name($schemaNode)"/> : schema : <xsl:value-of select="$schemaNode/text()"/></xsl:message>
          </xsl:if>

          <xsl:value-of select="$schemaNode/text()"/>

        </xsl:if>

      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>



  <xsl:template name="handleOneElement">
    <xsl:param name="name"/>
    <xsl:param name="schemaNode"/>
    <xsl:param name="dcmObjectCM"/>
    <xsl:param name="pathParent"/>
    <xsl:param name="marker"/>

    <xsl:variable name="result">
      <xsl:element name="{$name}">

        <xsl:call-template name="rewriteAttributes">
          <xsl:with-param name="schemaNode" select="$schemaNode"/>
          <xsl:with-param name="inputNode" select="."/>
        </xsl:call-template>

        <xsl:choose>
          <xsl:when test="$schemaNode/*">

            <!-- Copy/create any child nodes dictated by the schema. -->
            <xsl:variable name="this" select="."/>
            <xsl:for-each select="$schemaNode/*">
              <xsl:copy-of select="om:rewriteAccordingToSchema(., $this/*, $dcmObjectCM, concat($pathParent, $name, '/'))"/>
            </xsl:for-each>

          </xsl:when>
          <xsl:otherwise>

            <!-- No child elements, so it seems like some textual content might be in order. -->
            <xsl:call-template name="rewriteText">
              <xsl:with-param name="schemaNode" select="$schemaNode"/>
              <xsl:with-param name="inputNode" select="."/>
            </xsl:call-template>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:element>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$debugXML">
        <xsl:element name="{$marker}">
          <xsl:copy-of select="$result"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$result"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- 
    Given an element that contains child elements (e.g. PolicyMaps containing Match and Rule elements), and a
    dcm:vector-* element that contains qualifier attributes, determine whether the supplied element meets those
    qualifications.  For example, suppose that the input is:
        <PolicyMaps>
            <Match class="Matching">abc</Match>
            <Rule class="StylePolicyRule">def</Rule>
        </PolicyMaps>
    and the qualifier element is:
        <dcm:vector-replace class="PolicyMaps" match="abc" rule="def"/>
    then
        1. The function would compare the name of the root element <PolicyMaps> to 
           <dcm:vector-replace class="PolicyMaps"> and find that they match.
        2. The function would compare the <dcm:vector-replace match="abc"> to the
           <Match class="Matching">abc</Match> and find that they match.
        3. The function would compare the <dcm:vector-replace rule="def"> to the
           <Rule class="StylePolicyRule">def</Rule> and find that they match.
    so the function would return true().  Should any of these tests fail then it would
    return false().
    
    Global operations (e.g. <dcm:vector-delete class=""/>) always fail.
  -->
  <func:function name="om:isQualified">
    <xsl:param name="elTest"/>
    <xsl:param name="dcmVectorOp"/>
    <xsl:choose>
      <xsl:when test="name($elTest) = $dcmVectorOp/@class">

        <xsl:if test="$debug">
          <xsl:message> om:isQualified : <xsl:value-of select="name($dcmVectorOp)"/> : qualifies by class, checking other attributes.</xsl:message>
        </xsl:if>

        <xsl:choose>
          <xsl:when test="$dcmVectorOp/@*[name() != 'class']">
            
            
            <xsl:choose>
              <xsl:when test="$dcmVectorOp/@content">
                
                <!-- Must match by content. -->
                <xsl:choose>
                  <xsl:when test="$dcmVectorOp/@content = normalize-space($elTest)">
                  
                    <xsl:if test="$debug">
                      <xsl:message> om:isQualifiedImpl : qualifies by content(<xsl:value-of select="$dcmVectorOp/@content"/>).</xsl:message>
                    </xsl:if>
                    
                    <!-- Matches by content, so check the remaining attributes, if any. -->
                    <func:result select="om:isQualifiedImpl($elTest, $dcmVectorOp/@*[name() != 'class' and name() != 'content'])"/>
                    
                  </xsl:when>
                  <xsl:otherwise>
                    
                    <xsl:if test="$debug">
                      <xsl:message> om:isQualified : <xsl:value-of select="name($dcmVectorOp)"/> : qualifies by class but fails to have the needed content(<xsl:value-of select="$dcmVectorOp/@content"/>).</xsl:message>
                    </xsl:if>
                    
                    <func:result select="false()"/>
                    
                  </xsl:otherwise>
                </xsl:choose>
                
              </xsl:when>
              <xsl:otherwise>
                
                <!-- Matching by content is not required, so just check any remaining attributes. -->
                <func:result select="om:isQualifiedImpl($elTest, $dcmVectorOp/@*[name() != 'class'])"/>
                
              </xsl:otherwise>
            </xsl:choose>
            
            

          </xsl:when>
          <xsl:otherwise>

            <xsl:if test="$debug">
              <xsl:message> om:isQualified : <xsl:value-of select="name($dcmVectorOp)"/> : qualifies by class but fails because it is global.</xsl:message>
            </xsl:if>

            <!-- This is a global vector operation, not one specific to a particular entry. -->
            <func:result select="false()"/>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:when>
      <xsl:otherwise>

        <xsl:if test="$debug">
          <xsl:message> om:isQualified : <xsl:value-of select="name($dcmVectorOp)"/> : failed to qualify by class.</xsl:message>
        </xsl:if>

        <func:result select="false()"/>

      </xsl:otherwise>
    </xsl:choose>
  </func:function>

  <!-- 
    Test whether the elTest nodeset is qualified according to the first attribute.  If so
    then recurse and test the next attribute.
  -->
  <func:function name="om:isQualifiedImpl">
    <xsl:param name="elTest"/>
    <xsl:param name="listAttrs"/>
    <xsl:choose>
      <xsl:when test="$listAttrs">

        <!-- See if this attribute matches the value of an element. -->
        <xsl:variable name="attrName" select="translate(name($listAttrs[1]), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        <xsl:choose>
          <xsl:when test="$elTest/*[$attrName = translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')] = $listAttrs[1]">

            <xsl:if test="$debug">
              <xsl:message> om:isQualifiedImpl : qualifies by <xsl:value-of select="$attrName"/>.</xsl:message>
            </xsl:if>

            <!-- The required element was present and had the same value as the qualifier attribute.  Continue checking. -->
            <func:result select="om:isQualifiedImpl($elTest, $listAttrs[2])"/>

          </xsl:when>
          <xsl:otherwise>

            <xsl:if test="$debug">
              <xsl:message> om:isQualifiedImpl : failed to qualify by <xsl:value-of select="$attrName"/> : <xsl:value-of select="concat($attrName, '=', $listAttrs[1])"/> versus <xsl:value-of select="$elTest/*[$attrName = translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')]"/>.</xsl:message>
            </xsl:if>

            <!-- The required element wasn't present or had a different value than the qualifier attribute. -->
            <func:result select="false()"/>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:when>
      <xsl:otherwise>

        <!-- Nothing in the list, so we are happy since all qualifier attributes were matched. -->
        <func:result select="true()"/>

      </xsl:otherwise>
    </xsl:choose>
  </func:function>





  <!-- *************************************************************************************** -->
  <!-- ********                               DEBUG CODE                              ******** -->
  <!-- *************************************************************************************** -->




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
    <xsl:value-of select="'true'"/>
  </xsl:template>

  <xsl:template match="node()" mode="local:isDocumentNode">
    <xsl:value-of select="'false'"/>
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
