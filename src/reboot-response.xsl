<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">

	<xsl:output method="text" />

	<xsl:template match="//*[local-name()='Status']">
        <xsl:variable name="status" select="."/>
		<xsl:choose>
			<xsl:when test="$status='ok'">
				<xsl:value-of
					select="concat('[OK] reboot status: ', $status)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat('[FAIL] reboot status: ', $status)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>