<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:dcm="urn:datapower:configuration:manager">

	<xsl:param name="domain" />

	<xsl:template match="/">
		<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">
			<env:Body>
				<dp:request domain="{$domain}"
					xmlns:dp="http://www.datapower.com/schemas/management">
					<dp:get-status class="ObjectStatus" />
				</dp:request>
			</env:Body>
		</env:Envelope>
	</xsl:template>

</xsl:stylesheet>