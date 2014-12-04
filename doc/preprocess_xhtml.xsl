<?xml version='1.0'?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:xh="http://www.w3.org/1999/xhtml" version="1.0" exclude-result-prefixes="xh">

  <xsl:output method="xml" indent="no" version="1.0" encoding="UTF-8" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" omit-xml-declaration="no" />

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>



  <xsl:template match="xh:caption" mode="tot">
    <div class="tot-line">
      <xsl:call-template name="toc-link" />
    </div>
  </xsl:template>



  <xsl:template match="xh:div[@class = 'toc' and not(node())]">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <h1 />
      <div class="toc-main">
        <xsl:apply-templates select="//xh:div[@class = 'main']//xh:h1 |             //xh:div[@class = 'main']//xh:h2 |             //xh:div[@class = 'main']//xh:h3" mode="toc" />
      </div>
      <div class="toc-back">
        <xsl:apply-templates select="//xh:div[@class = 'back']//xh:h1 |             //xh:div[@class = 'back']//xh:h2 |             //xh:div[@class = 'back']//xh:h3" mode="toc" />
      </div>
    </xsl:copy>
  </xsl:template>



  <xsl:template match="xh:div[@class = 'tof' and not(node())]">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <h1 />
      <div class="tof-main">
        <xsl:apply-templates select="//xh:div[@class = 'main']//xh:div[@class = 'img-caption']" mode="tof" />
      </div>
      <div class="tof-back">
        <xsl:apply-templates select="//xh:div[@class = 'back']//xh:div[@class = 'img-caption']" mode="tof" />
      </div>
    </xsl:copy>
  </xsl:template>



  <xsl:template match="xh:div[@class = 'tot' and not(node())]">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <h1 />
      <div class="tot-main">
        <xsl:apply-templates select="//xh:div[@class = 'main']//xh:caption" mode="tot" />
      </div>
      <div class="tot-back">
        <xsl:apply-templates select="//xh:div[@class = 'back']//xh:caption" mode="tot" />
      </div>
    </xsl:copy>
  </xsl:template>



  <xsl:template match="xh:caption | xh:h1 | xh:h2 | xh:h3 | xh:div[@class = 'img-caption']">
    <xsl:copy>
      <xsl:attribute name="id">
        <xsl:value-of select="generate-id()" />
      </xsl:attribute>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>



  <xsl:template match="xh:h1 | xh:h2 | xh:h3" mode="toc">
    <div class="{concat('toc-', name())}">
      <xsl:call-template name="toc-link" />
    </div>
  </xsl:template>



  <xsl:template match="xh:div[@class = 'img-caption']" mode="tof">
    <div class="tof-line">
      <xsl:call-template name="toc-link" />
    </div>
  </xsl:template>



  <xsl:template match="@id | @xml:id" mode="href">
    <xsl:attribute name="href">
      <xsl:text>#</xsl:text>
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>



  <xsl:template match="@id | @xml:id" mode="class">
    <xsl:attribute name="class">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>



  <!-- Named templates. -->

  <xsl:template name="toc-link">
    <a href="{concat('#', generate-id())}">
      <xsl:apply-templates select="@id | @xml:id" mode="href" />
      <xsl:apply-templates select="node()" />
    </a>
    <span class="leader" />
    <span class="page-ref">
      <span class="{generate-id()}">
        <xsl:apply-templates select="@id | @xml:id" mode="class" />
      </span>
    </span>
  </xsl:template>

</xsl:transform>
