<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:dcm="urn:datapower:configuration:manager" xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">

	<xsl:output method="text" />

	<xsl:param name="manifest-file" />
	<xsl:variable name="manifest" select="document($manifest-file)" />

	<xsl:template match="/">
		<xsl:variable name="root" select="/" />
		<xsl:for-each select="$manifest/dcm:definition/dcm:object-state-manifest/dcm:object-state">
			<xsl:variable name="name" select="@name" />
			<xsl:variable name="class" select="@class" />
			<xsl:variable name="required-op-state" select="@op-state" />
			<xsl:variable name="op-state"
				select="$root/env:Envelope/env:Body/dp:response/dp:status/ObjectStatus/OpState[../Name/text()=$name and ../Class/text()=$class]" />
			<xsl:choose>
				<xsl:when test="$op-state=$required-op-state">
					<xsl:value-of
						select="concat('[OK](', $name, ') expected state: ', $required-op-state, ', found: ', $op-state, '; ')" />
				</xsl:when>
				<xsl:when test="$op-state!=$required-op-state">
					<xsl:value-of
						select="concat('[FAIL](', $name, ') expected state: ', $required-op-state, ', found: ', $op-state, '; ')" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat('[FAIL] missing object: ', $name, '; ')" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<xsl:text>object status checks complete</xsl:text>
	</xsl:template>

</xsl:stylesheet>