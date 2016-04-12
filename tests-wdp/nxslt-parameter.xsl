<?xml version="1.0" encoding="UTF-8"?>
<!-- 

  This stylesheet prints out a couple of passed-in parameters.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

  <xsl:param name="param1"/>
  <xsl:param name="param2" select="'default-param-2'"/>
  
  <xsl:template match="/">
    <xsl:message>param1=<xsl:value-of select="$param1"/></xsl:message>
    <xsl:message>param2=<xsl:value-of select="$param2"/></xsl:message>
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
