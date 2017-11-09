<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<xsl:param name="mode" />

	<xsl:template match="/">
		<!-- NB: always prefer to use the OLDEST AMP version to provide greatest 
			backwards compatibility with older firmware versions unless there is a good 
			reason not to -->
		<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
			xmlns:ns="http://www.datapower.com/schemas/appliance/management/1.0">
			<soapenv:Header />
			<soapenv:Body>
				<ns:RebootRequest>
					<ns:Mode>
						<xsl:value-of select="$mode" />
					</ns:Mode>
				</ns:RebootRequest>
			</soapenv:Body>
		</soapenv:Envelope>
	</xsl:template>

</xsl:stylesheet>