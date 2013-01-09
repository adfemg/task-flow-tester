<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:emg="http://adf.emg.org/tftester">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="emg:ParamValueObject">
    <xsl:element name="ParamValueObject" namespace="http://adf.emg.org/tftester">
      <xsl:apply-templates select="@*[local-name()!='valueAsString']"/>
      <xsl:if test="@valueAsString">
        <xsl:element name="valueAsString" namespace="http://adf.emg.org/tftester">
           <xsl:value-of select="@valueAsString"/>
        </xsl:element>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
