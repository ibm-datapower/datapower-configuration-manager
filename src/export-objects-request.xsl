<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:dcm="urn:datapower:configuration:manager">

	<xsl:param name="domain" />

	<xsl:template match="export-object">
		<dp:object name="{@name}" class="{@class}" ref-objects="{@ref-objects}"
			ref-files="{@ref-files}" />
	</xsl:template>

	<xsl:template match="/">
		<xsl:apply-templates
			select="/dcm:definition/dcm:export-manifest/export-object" />
	</xsl:template>

</xsl:stylesheet>