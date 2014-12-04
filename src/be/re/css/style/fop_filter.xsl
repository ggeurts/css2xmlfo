<?xml version='1.0'?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

  <xsl:output method="xml" indent="no" version="1.0" encoding="UTF-8" omit-xml-declaration="no" />



  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>



  <xsl:template match="fo:list-item-body/fo:block/@space-before" />
  <xsl:template match="@page-break-after" />
  <xsl:template match="@page-break-before" />
  <xsl:template match="@page-break-inside" />
  <xsl:template match="@text-transform" />



  <xsl:template match="@font-weight[. = 'bolder']">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:template>



  <xsl:template match="@margin-bottom[. = '0']">
    <xsl:attribute name="space-after">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:block/@margin-bottom | fo:block-container/@margin-bottom |       fo:table/@margin-bottom">
    <xsl:attribute name="space-after">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="@margin-left[. = '0']" priority="1">
    <xsl:attribute name="start-indent">
      <xsl:text>inherited-property-value(start-indent)</xsl:text>
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:block/@margin-left | fo:block-container/@margin-left |       fo:table/@margin-left">
    <xsl:attribute name="start-indent">
      <xsl:text>inherited-property-value(start-indent)</xsl:text>
      <xsl:if test=". != 'auto'">
        <xsl:text>+</xsl:text>
        <xsl:value-of select="." />
      </xsl:if>
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:table-cell">
    <fo:table-cell start-indent="0pt" end-indent="0pt">
      <xsl:apply-templates select="@* | node()" />
    </fo:table-cell>
  </xsl:template>



  <xsl:template match="@margin-right[. = '0']" priority="1">
    <xsl:attribute name="end-indent">
      <xsl:text>inherited-property-value(end-indent)</xsl:text>
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:block/@margin-right |       fo:block-container/@margin-right |       fo:table/@margin-right">
    <xsl:attribute name="end-indent">
      <xsl:text>inherited-property-value(end-indent)</xsl:text>
      <xsl:if test=". != 'auto'">
        <xsl:text>+</xsl:text>
        <xsl:value-of select="." />
      </xsl:if>
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="@margin-top[. = '0']">
    <xsl:attribute name="space-before">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:block/@margin-top | fo:block-container/@margin-top |       fo:table/@margin-top">
    <xsl:attribute name="space-before">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="@padding-bottom[. = '0']">
    <xsl:attribute name="padding-bottom">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="@padding-left[. = '0']">
    <xsl:attribute name="padding-left">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="@padding-right[. = '0']">
    <xsl:attribute name="padding-right">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="@padding-top[. = '0']">
    <xsl:attribute name="padding-top">0pt</xsl:attribute>
  </xsl:template>



  <xsl:template match="@provisional-distance-between-starts">
    <xsl:attribute name="provisional-distance-between-starts">2em</xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:table">
    <fo:table width="100%">
      <xsl:apply-templates select="@*" /> <!-- Can override width. -->
      <xsl:attribute name="table-layout">fixed</xsl:attribute>
      <xsl:apply-templates select="(.//fo:table-row)[1]/fo:table-cell" mode="table-no-columns" />
      <xsl:apply-templates select="node()" />
    </fo:table>
  </xsl:template>



  <xsl:template match="fo:table/@width[. = 'auto']">
    <xsl:attribute name="width">100%</xsl:attribute>
  </xsl:template>



  <xsl:template match="fo:table[not(fo:table-column)]//fo:table-cell" mode="table-no-columns">
    <fo:table-column column-width="proportional-column-width(1)" />
  </xsl:template>



  <xsl:template match="text()" mode="table-no-columns" />



  <xsl:template match="fo:table-column">
    <fo:table-column column-width="proportional-column-width(1)">
      <xsl:apply-templates select="@*" />
    </fo:table-column>
  </xsl:template>



  <xsl:template match="fo:table-cell//fo:block-container">
    <fo:block>
      <xsl:apply-templates select="@* | node()" />
    </fo:block>
  </xsl:template>

</xsl:transform>
