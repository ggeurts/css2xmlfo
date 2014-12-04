<?xml version='1.0'?>
<xsl:transform xmlns:css="http://www.w3.org/1998/CSS" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

  <xsl:param name="column-count">1</xsl:param>
  <xsl:param name="country">GB</xsl:param>
  <xsl:param name="font-family">serif</xsl:param>
  <xsl:param name="font-size">none</xsl:param>
  <xsl:param name="language">en</xsl:param>
  <xsl:param name="link-color">black</xsl:param>
  <xsl:param name="odd-even-shift">10mm</xsl:param>
  <xsl:param name="orientation">portrait</xsl:param>
  <xsl:param name="paper-margin-bottom">10mm</xsl:param>
  <xsl:param name="paper-margin-left">25mm</xsl:param>
  <xsl:param name="paper-margin-right">25mm</xsl:param>
  <xsl:param name="paper-margin-top">10mm</xsl:param>
  <xsl:param name="paper-mode">onesided</xsl:param>
    <!-- Can be "twosided" or "onesided". -->
  <xsl:param name="paper-size">a4</xsl:param>
  <xsl:param name="rule-thickness">0.2pt</xsl:param>



  <xsl:variable name="actual-font-size">
    <xsl:choose>
      <xsl:when test="$font-size != 'none'">
        <xsl:value-of select="$font-size" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$paper-size = 'a5' or $paper-size = 'b5'">
            <xsl:text>10pt</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>11pt</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="portrait-paper-height">
    <xsl:choose>
      <xsl:when test="$paper-size = 'a0'">
        <xsl:text>1188mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a1'">
        <xsl:text>840mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a2'">
        <xsl:text>594mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a3'">
        <xsl:text>420mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a4'">
        <xsl:text>297mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a5'">
        <xsl:text>210mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'b5'">
        <xsl:text>250mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'executive'">
        <xsl:text>11in</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'letter'">
        <xsl:text>11in</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'legal'">
        <xsl:text>14in</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="no">Unknown paper-size, taking a4</xsl:message>
        <xsl:text>297mm</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="portrait-paper-width">
    <xsl:choose>
      <xsl:when test="$paper-size = 'a0'">
        <xsl:text>840mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a1'">
        <xsl:text>594mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a2'">
        <xsl:text>420mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a3'">
        <xsl:text>297mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a4'">
        <xsl:text>210mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'a5'">
        <xsl:text>148mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'b5'">
        <xsl:text>176mm</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'executive'">
        <xsl:text>7.25in</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'letter'">
        <xsl:text>8.5in</xsl:text>
      </xsl:when>
      <xsl:when test="$paper-size = 'legal'">
        <xsl:text>8.5in</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>210mm</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="paper-height">
    <xsl:choose>
      <xsl:when test="$orientation = 'portrait'">
        <xsl:value-of select="$portrait-paper-height" />
      </xsl:when>
      <xsl:when test="$orientation = 'landscape'">
        <xsl:value-of select="$portrait-paper-width" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="no">Unknown orientation, taking portrait</xsl:message>
        <xsl:value-of select="$portrait-paper-height" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="paper-width">
    <xsl:choose>
      <xsl:when test="$orientation = 'portrait'">
        <xsl:value-of select="$portrait-paper-width" />
      </xsl:when>
      <xsl:when test="$orientation = 'landscape'">
        <xsl:value-of select="$portrait-paper-height" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$portrait-paper-width" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="writing-mode">lr-tb</xsl:variable>



  <!-- Page setup. -->

  <xsl:template match="/">
    <xsl:apply-templates select="*" mode="setup" />
  </xsl:template>



  <xsl:template match="/css:root[not(css:pages)]" mode="setup">
    <fo:root font-selection-strategy="character-by-character" line-height-shift-adjustment="disregard-shifts" country="{$country}" font-family="{$font-family}" font-size="{$actual-font-size}" language="{$language}">
      <xsl:apply-templates select="@xml:lang" />
      <fo:layout-master-set>
        <fo:simple-page-master master-name="blank" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left}" margin-right="{$paper-margin-right}">
          <fo:region-body margin-top="0mm" margin-bottom="0mm" margin-left="0mm" margin-right="0mm" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="even" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left} - {$odd-even-shift}" margin-right="{$paper-margin-right} + {$odd-even-shift}">
          <xsl:call-template name="region-body" />
          <xsl:call-template name="region-before">
            <xsl:with-param name="region-name">region-before-even</xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="region-after" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="odd" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left} + {$odd-even-shift}" margin-right="{$paper-margin-right} - {$odd-even-shift}">
          <xsl:call-template name="region-body" />
          <xsl:call-template name="region-before">
            <xsl:with-param name="region-name">region-before-odd</xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="region-after" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="odd-first" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left} + {$odd-even-shift}" margin-right="{$paper-margin-right} - {$odd-even-shift}">
          <xsl:call-template name="region-body" />
          <xsl:call-template name="region-after" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="onesided" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left}" margin-right="{$paper-margin-right}">
          <xsl:call-template name="region-body" />
          <xsl:call-template name="region-before">
            <xsl:with-param name="region-name">region-before-even</xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="region-after" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="onesided-first" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}" margin-left="{$paper-margin-left}" margin-right="{$paper-margin-right}">
          <xsl:call-template name="region-body" />
          <xsl:call-template name="region-after" />
        </fo:simple-page-master>

        <fo:simple-page-master master-name="title" writing-mode="{$writing-mode}" page-height="{$paper-height}" page-width="{$paper-width}" margin-top="{$paper-margin-top}" margin-bottom="{$paper-margin-bottom}">
          <xsl:if test="$paper-mode='onesided'">
            <xsl:attribute name="margin-left">
              <xsl:value-of select="$paper-margin-left" />
            </xsl:attribute>
            <xsl:attribute name="margin-right">
              <xsl:value-of select="$paper-margin-right" />
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="$paper-mode='twosided'">
            <xsl:attribute name="margin-left">
              <xsl:value-of select="concat($paper-margin-left, ' + ', $odd-even-shift)" />
            </xsl:attribute>
            <xsl:attribute name="margin-right">
              <xsl:value-of select="concat($paper-margin-left, ' - ', $odd-even-shift)" />
            </xsl:attribute>
          </xsl:if>
          <fo:region-body margin-top="0mm" margin-bottom="0mm" margin-left="0mm" margin-right="0mm" />
        </fo:simple-page-master>

        <fo:page-sequence-master master-name="document">
          <fo:repeatable-page-master-alternatives>
            <xsl:if test="$paper-mode='twosided'">
              <fo:conditional-page-master-reference odd-or-even="even" master-reference="even" />
              <fo:conditional-page-master-reference odd-or-even="odd" page-position="first" master-reference="odd-first" />
              <fo:conditional-page-master-reference odd-or-even="odd" page-position="any" master-reference="odd" />
            </xsl:if>

            <xsl:if test="$paper-mode='onesided'">
              <fo:conditional-page-master-reference page-position="first" master-reference="onesided-first" />
              <fo:conditional-page-master-reference page-position="any" master-reference="onesided" />
            </xsl:if>

            <fo:conditional-page-master-reference blank-or-not-blank="blank" page-position="any" master-reference="blank" />
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>

      <fo:page-sequence format="1" initial-page-number="1" master-reference="document">
        <xsl:call-template name="page-style" />
        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates select="css:page-sequence/*" />
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>



  <!-- Named templates. -->

  <xsl:template name="footnote-separator">
    <fo:block>
      <fo:leader leader-length="41%" leader-pattern="rule" rule-style="solid" rule-thickness="{$rule-thickness}" />
    </fo:block>
  </xsl:template>



  <xsl:template name="page-style">
    <fo:static-content flow-name="region-before-even">
      <xsl:call-template name="running-head">
        <xsl:with-param name="align">start</xsl:with-param>
      </xsl:call-template>
    </fo:static-content>

    <fo:static-content flow-name="region-before-odd">
      <xsl:call-template name="running-head">
        <xsl:with-param name="align">end</xsl:with-param>
      </xsl:call-template>
    </fo:static-content>

    <fo:static-content flow-name="xsl-footnote-separator">
      <xsl:call-template name="footnote-separator" />
    </fo:static-content>

    <fo:static-content flow-name="xsl-region-after">
      <xsl:call-template name="running-foot" />
    </fo:static-content>
  </xsl:template>



  <xsl:template name="region-after">
    <fo:region-after display-align="before" extent="10mm" />
  </xsl:template>



  <xsl:template name="region-before">
    <xsl:param name="region-name" />

    <fo:region-before display-align="after" extent="10mm" region-name="{$region-name}" />
  </xsl:template>



  <xsl:template name="region-body">
    <fo:region-body column-count="{$column-count}" margin-bottom="15mm" margin-left="0mm" margin-right="0mm" margin-top="15mm" />
  </xsl:template>



  <xsl:template name="running-head">
    <xsl:param name="align" />
    <fo:block font-style="oblique" text-align="{$align}" text-transform="uppercase">
      <fo:retrieve-marker retrieve-class-name="component" retrieve-position="first-starting-within-page" retrieve-boundary="document" />
    </fo:block>
  </xsl:template>



  <xsl:template name="running-foot">
    <fo:block text-align="center">
      <fo:page-number />
    </fo:block>
  </xsl:template>

</xsl:transform>
