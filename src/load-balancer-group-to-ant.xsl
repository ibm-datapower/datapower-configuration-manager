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

  This stylesheet generates an Ant script that will either create a fresh load balancer group object
  or ensure that an existing load balancer group object contains the specified hosts.

  The input to the stylesheet is a dcm:definition in this pattern:

  <dcm:definition>
    <dcm:loadbalancergroup name="..."
      [ alg='...' ]
      [ enabled="true|false" ]
      [ retrieve-info="true|false" ]
      [ damp="n" ]
      [ never-return-sick-member="true|false" ]
      [ try-every-server-before-failing="true|false" ]
      [ masquerade-member="true|false" ]
      [ application-routing="true|false" ]
      [ transport="..." ]
      [ websphere-cell-config="..." ]
      [ wlm-group="..." ] >
      <dcm:member server="..." [ weight="n" ] [ enabled="true|false ] [ port="n" ] [ health-port="n" ] "/>
      ...
      [
        <dcm:health-checks [ enabled="true|false" ]
          [ uri="..." ]
          [ port="n" ]
          [ ssl="Standard, LDAP, IMSConnect, TCPConnectionType, on, off" ]
          [ post="true|false" ]
          [ input="..." ]
          [ timeout="n" ]
          [ frequency="n" ]
          [ xpath = "..." ]
          [ filter = "..." ]
          [ ssl-proxy-profile = "..." ]
          [ enforce-timeout = "true|false" ]
          [ independent-checks = "true|false" ]
		  [ ssl-client = "..." ]/>
      ]
      [
        <dcm:affinity [ enabled="true|false" ]
          [ cookie-name="..." ]
          [ path="..." ]
          [ domain="..." ]
          [ wlm-override="true|false" ]
          [ mode="active|activeConditional" ]>
          [ secure="on|off" ]>
          [ httponly="on|off" ]>
          <dcm:monitored cookie-name="..."/>
        </dcm:affinity>
      ]
    </dcm:loadbalancergroup>
  </dcm:definition>

  Defaults:

    @alg = round-robin
    @enabled = true
    @retrieve-info = false
    @damp = 120
    @never-return-sick-member = false
    @try-every-server-before-failing = false
    @masquerade-member = false
    @transport = http

    @weight = 1
    @enabled = true
    @port = 0
    @health-port = 0

    @enabled = true
    @uri = /
    @port = 80
    @ssl = Standard
    @post = true
    @input = store:///healthcheck.xml
    @timeout = 10
    @frequency = 180
    @xpath = /
    @filter = store:///healthcheck.xml
    @ssl-proxy-profile = <none>
    @enforce-timeout = false
    @independent-checks = false

    @enabled = true
    @cookie-name = DPJSESSIONID
    @path = /
    @domain = .datapower.com
    @wlm-override = false
    @mode = activeConditional
    @secure = off
    @httponly = off

  The "template-file" stylesheet parameter is the name of an Ant script template file (e.g. _quickie.template.ant.xml).

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


  <xsl:param name="template-file"/> <!-- filename of the Ant script template -->
  <xsl:param name="firmware-version"/> <!-- e.g. XI52.7.2.0.0 -->

  <xsl:include href="util-merge-template.xsl"/>
  <xsl:include href="util-mutate-names.xsl"/>


  <xsl:variable name="sevenTwoOrLater">
    <xsl:variable name="rawNumber" select="substring-after($firmware-version, '.')"/>
    <xsl:variable name="major" select="number(substring-before($rawNumber, '.'))"/>
    <xsl:variable name="minor" select="number(substring-before(substring-after($rawNumber, '.'), '.'))"/>
    <xsl:choose>
      <xsl:when test="$major >= 7 and $minor >= 2">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>


  <xsl:template match="/">

    <!-- Load the Ant script template. -->
    <xsl:variable name="template" select="document($template-file)"/>
    <xsl:choose>
      <xsl:when test="$template/project">

        <!-- Process the dcm:definition input. -->
        <xsl:variable name="results" select="exslt:node-set(local:processDefinition(.))"/>
        <xsl:choose>
          <xsl:when test="$results//dcm:error">

            <!-- Show the errors and terminate. -->
            <xsl:for-each select="$results//dcm:error">
		<xsl:message>
		    <xsl:value-of select="."/>
		</xsl:message>
            </xsl:for-each>
            <xsl:message terminate="yes">Failed to generate internal template due to errors listed above.</xsl:message>

          </xsl:when>
          <xsl:when test="$results/generated/antscript/*">

            <!-- Successfully generated ant script content without encountering any errors. Merge that content into the template. -->
            <xsl:copy-of select="dtat:mergeTemplate($template, $results)"/>

          </xsl:when>
          <xsl:otherwise>

            <xsl:message terminate="yes">Odd.  Failed to generate an internal template and there isn't any apparent reason.</xsl:message>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:when>
      <xsl:otherwise>

        <xsl:message terminate="yes">Failed to load the Ant script template <xsl:value-of select="$template-file"/>!</xsl:message>

      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
    Generate Ant script content based on the supplied dcm:definition.  The result may contain
    dcm:error elements that describe various errors in the input definition.
  -->
  <func:function name="local:processDefinition">
    <xsl:param name="dcmdef"/>
    <func:result>

      <xsl:element name="generated">
        <xsl:element name="antscript">

          <xsl:choose>
            <xsl:when test="$dcmdef/dcm:definition">

              <xsl:choose>
                <xsl:when test="$dcmdef/dcm:definition/dcm:loadbalancergroup">

                  <!-- Generate ant script content for each load balancer group -->
                  <xsl:apply-templates select="$dcmdef/dcm:definition/dcm:loadbalancergroup" mode="processDefinition"/>

                </xsl:when>
                <xsl:otherwise>

                  <xsl:element name="dcm:error">This file doesn't contain a dcm:loadbalancergroup element!</xsl:element>

                </xsl:otherwise>
              </xsl:choose>

            </xsl:when>
            <xsl:otherwise>

              <xsl:element name="dcm:error">This file doesn't contain a dcm:definition element!</xsl:element>

            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:element>

    </func:result>
  </func:function>


  <xsl:template match="dcm:loadbalancergroup[@name != '']" mode="processDefinition">

    <!-- Generate the content of a SetConfig to create/overwrite the load balancer group object. -->
    <xsl:variable name="config">
      <xsl:element name="LoadBalancerGroup">
        <xsl:attribute name="name">
          <xsl:value-of select="@name"/>
        </xsl:attribute>

        <xsl:element name="mAdminState">
          <xsl:choose>
            <xsl:when test="not(@enabled) or @enabled='true'">
              <xsl:value-of select="'enabled'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'disabled'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="Algorithm">
          <xsl:choose>
            <xsl:when test="@alg != ''">
              <xsl:value-of select="@alg"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'round-robin'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="RetrieveInfo">
          <xsl:choose>
            <xsl:when test="not(@retrieve-info) or @retrieve-info!='true'">
              <xsl:value-of select="'off'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'on'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="WLMRetrieval">
          <xsl:value-of select="'use-websphere'"/>
        </xsl:element>

        <xsl:if test="@websphere-cell-config">
          <xsl:element name="WebSphereCellConfig">
            <xsl:value-of select="@websphere-cell-config"/>
          </xsl:element>
        </xsl:if>

        <xsl:if test="@wlm-group">
          <xsl:element name="WLMGroup">
            <xsl:value-of select="@wlm-group"/>
          </xsl:element>
        </xsl:if>

        <xsl:element name="WLMTransport">
          <xsl:choose>
            <xsl:when test="@transport != ''">
              <xsl:value-of select="@transport"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'http'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="Damp">
          <xsl:choose>
            <xsl:when test="@damp != ''">
              <xsl:value-of select="@damp"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'120'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="NeverReturnSickMember">
          <xsl:choose>
            <xsl:when test="not(@never-return-sick-member) or @never-return-sick-member != 'true'">
              <xsl:value-of select="'off'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'on'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:for-each select="dcm:member">

          <xsl:element name="LBGroupMembers">

            <xsl:element name="Server">
              <xsl:value-of select="@server"/>
            </xsl:element>

            <xsl:element name="Weight">
              <xsl:choose>
                <xsl:when test="@weight != ''">
                  <xsl:value-of select="@weight"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'1'"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>

            <xsl:element name="MappedPort">
              <xsl:choose>
                <xsl:when test="@port != ''">
                  <xsl:value-of select="@port"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'0'"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>

            <xsl:element name="Activity"/>

            <xsl:element name="HealthPort">
              <xsl:choose>
                <xsl:when test="@health-port != ''">
                  <xsl:value-of select="@health-port"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'0'"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>

            <xsl:element name="LBMemberState">
              <xsl:choose>
                <xsl:when test="not(@enabled) or @enabled='true'">
                  <xsl:value-of select="'enabled'"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="'disabled'"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>

          </xsl:element>

        </xsl:for-each>

        <xsl:element name="TryEveryServerBeforeFailing">
          <xsl:choose>
            <xsl:when test="not(@try-every-server-before-failing) or @try-every-server-before-failing != 'true'">
              <xsl:value-of select="'off'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'on'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="LBGroupChecks">

          <xsl:variable name="checks" select="dcm:health-checks[1]"/>

          <xsl:element name="Active">
            <xsl:choose>
              <xsl:when test="not($checks/@enabled) or $checks/@enabled != 'true'">
                <xsl:value-of select="'off'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'on'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="URI">
            <xsl:choose>
              <xsl:when test="$checks/@uri != ''">
                <xsl:value-of select="$checks/@uri"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'/'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Port">
            <xsl:choose>
              <xsl:when test="$checks/@port != ''">
                <xsl:value-of select="$checks/@port"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'80'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="SSL">
            <xsl:choose>
              <xsl:when test="$checks/@ssl != ''">
                <xsl:value-of select="$checks/@ssl"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'Standard'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Post">
            <xsl:choose>
              <xsl:when test="not($checks/@post) or $checks/@post = 'true'">
                <xsl:value-of select="'on'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'off'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Input">
            <xsl:choose>
              <xsl:when test="$checks/@input != ''">
                <xsl:value-of select="$checks/@input"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'store:///healthcheck.xml'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Timeout">
            <xsl:choose>
              <xsl:when test="$checks/@timeout != ''">
                <xsl:value-of select="$checks/@timeout"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'10'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Frequency">
            <xsl:choose>
              <xsl:when test="$checks/@frequency != ''">
                <xsl:value-of select="$checks/@frequency"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'180'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="XPath">
            <xsl:choose>
              <xsl:when test="$checks/@xpath != ''">
                <xsl:value-of select="$checks/@xpath"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'/'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="Filter">
            <xsl:choose>
              <xsl:when test="$checks/@filter != ''">
                <xsl:value-of select="$checks/@filter"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'store:///healthcheck.xsl'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="SSLProxyProfile">
            <xsl:if test="$checks/@ssl-proxy-profile != ''">
              <xsl:value-of select="$checks/@ssl-proxy-profile"/>
            </xsl:if>
          </xsl:element>

          <xsl:element name="EnforceTimeout">
            <xsl:choose>
              <xsl:when test="$checks/@enforce-timeout = 'true'">
                <xsl:value-of select="'on'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'off'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="IndependentChecks">
            <xsl:choose>
              <xsl:when test="$checks/@independent-checks = 'true'">
                <xsl:value-of select="'on'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'off'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:if test="$sevenTwoOrLater = 'true'">

            <xsl:element name="GatewayScriptChecks">
              <xsl:value-of select="'off'"/>
            </xsl:element>
            <xsl:element name="GatewayScriptReqMethod">
              <xsl:value-of select="'GET'"/>
            </xsl:element>
            <xsl:element name="GatewayScriptCustomReqMethod"/>
            <xsl:element name="GatewayScriptReqDoc"/>
            <xsl:element name="GatewayScriptReqContentType"/>
            <xsl:element name="GatewayScriptRspHandlerMetadata"/>
            <xsl:element name="GatewayScriptRspHandler"/>
            <xsl:element name="TCPConnectionType">
              <xsl:value-of select="'Full'"/>
            </xsl:element>
            <xsl:element name="SSLClientConfigType">
              <xsl:value-of select="'client'"/>
            </xsl:element>
    	    <xsl:element name="SSLClient">
              <xsl:if test="$checks/@ssl-client != ''">
                <xsl:value-of select="$checks/@ssl-client"/>
              </xsl:if>
    	    </xsl:element>
	   </xsl:if>
        </xsl:element>

        <xsl:element name="MasqueradeMember">
          <xsl:choose>
            <xsl:when test="not(@masquerade-member) or @masquerade-member != 'true'">
              <xsl:value-of select="'off'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'on'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:element name="ApplicationRouting">
          <xsl:choose>
            <xsl:when test="not(@application-routing) or @application-routing != 'true'">
              <xsl:value-of select="'off'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'on'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>

        <xsl:variable name="affinity" select="dcm:affinity[1]"/>

        <xsl:element name="LBGroupAffinityConf">

          <xsl:element name="EnableSA">
            <xsl:choose>
              <xsl:when test="not($affinity/@enabled) or $affinity/@enabled = 'true'">
                <xsl:value-of select="'on'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'off'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="InsertionCookieName">
            <xsl:choose>
              <xsl:when test="$affinity/@cookie-name != ''">
                <xsl:value-of select="$affinity/@cookie-name"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'DPJSESSIONID'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="InsertionPath">
            <xsl:choose>
              <xsl:when test="$affinity/@path != ''">
                <xsl:value-of select="$affinity/@path"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'/'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="InsertionDomain">
            <xsl:choose>
              <xsl:when test="$affinity/@domain != ''">
                <xsl:value-of select="$affinity/@domain"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'.datapower.com'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="AffinityWLMOverride">
            <xsl:choose>
              <xsl:when test="not($affinity/@wlm-override) or $affinity/@wlm-override != 'true'">
                <xsl:value-of select="'off'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'on'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:element name="AffinityMode">
            <xsl:choose>
              <xsl:when test="$affinity/@mode != ''">
                <xsl:value-of select="$affinity/@mode"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'activeConditional'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>

          <xsl:if test="$sevenTwoOrLater = 'true'">

            <xsl:element name="InsertionAttributes">
              <xsl:element name="secure">
                <xsl:choose>
                  <xsl:when test="$affinity/@secure != ''">
                    <xsl:value-of select="$affinity/@secure"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="'off'"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:element>
              <xsl:element name="httponly">
                <xsl:choose>
                  <xsl:when test="$affinity/@httponly != ''">
                    <xsl:value-of select="$affinity/@httponly"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="'off'"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:element>
            </xsl:element>

          </xsl:if>

        </xsl:element>

        <xsl:for-each select="$affinity/dcm:monitored[@cookie-name != '']">
          <xsl:element name="MonitoredCookies">
            <xsl:value-of select="@cookie-name"/>
          </xsl:element>
        </xsl:for-each>

      </xsl:element>

    </xsl:variable>

    <!-- Generate the ant script content to create the load balancer group object on DataPower. -->
    <xsl:element name="sequential">

      <xsl:element name="local">
        <xsl:attribute name="name">response-create</xsl:attribute>
      </xsl:element>
      <xsl:element name="local">
        <xsl:attribute name="name">success-create</xsl:attribute>
      </xsl:element>

      <xsl:element name="wdp">
        <xsl:attribute name="operation">SetConfig</xsl:attribute>
        <xsl:attribute name="successprop">success-create</xsl:attribute>
        <xsl:attribute name="responseprop">response-create</xsl:attribute>
        <xsl:attribute name="dumpinput">${dumpinput}</xsl:attribute>
        <xsl:attribute name="dumpoutput">${dumpoutput}</xsl:attribute>

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

          <xsl:element name="echo">Created the Load Balancer Group object <xsl:value-of select="@name"/>, encompassing <xsl:value-of select="count(dcm:member)"/> members.</xsl:element>

        </xsl:element>
        <xsl:element name="else">

          <xsl:element name="fail">
            <xsl:attribute name="message">Failed to create the Load Balancer Group object <xsl:value-of select="@name"/>.</xsl:attribute>
          </xsl:element>

        </xsl:element>
      </xsl:element>

    </xsl:element>

  </xsl:template>


  <xsl:template match="dcm:loadbalancergroup" mode="processDefinition">
    <xsl:element name="dcm:error">The <xsl:value-of select="name()"/> element must have a name="..." attribute.</xsl:element>
  </xsl:template>


</xsl:stylesheet>
