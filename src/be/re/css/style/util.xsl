<?xml version='1.0'?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template name="count-tokens">
    <xsl:param name="s" />

    <xsl:variable name="norm" select="normalize-space($s)" />

    <xsl:choose>
      <xsl:when test="string-length($norm) = 0">
        <xsl:value-of select="number(0)" />
      </xsl:when>
      <xsl:when test="contains($norm, ' ')">
        <xsl:variable name="value">
          <xsl:call-template name="count-tokens">
            <xsl:with-param name="s" select="substring-after($norm, ' ')" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="$value + 1" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(1)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template name="get-token">
    <xsl:param name="current-position" select="number(1)" />
    <xsl:param name="position" />
    <xsl:param name="s" />

    <xsl:if test="$position &lt; 1">
      <xsl:message terminate="yes">Position for get-token must be greater than 0.</xsl:message>
    </xsl:if>

    <xsl:variable name="norm" select="normalize-space($s)" />

    <xsl:choose>
      <xsl:when test="$current-position = $position">
        <xsl:choose>
          <xsl:when test="contains($norm, ' ')">
            <xsl:value-of select="substring-before($norm, ' ')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$norm" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($norm, ' ')">
            <xsl:call-template name="get-token">
              <xsl:with-param name="current-position" select="$current-position + 1" />
              <xsl:with-param name="position" select="$position" />
              <xsl:with-param name="s" select="substring-after($norm, ' ')" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template name="replace-string">
    <xsl:param name="s" />
    <xsl:param name="replace" />
    <xsl:param name="by" />

    <xsl:choose>
      <xsl:when test="contains($s, $replace)">
        <xsl:call-template name="replace-string">
          <xsl:with-param name="s" select="concat(substring-before($s, $replace), $by,               substring-after($s, $replace))" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$s" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template name="uri-last-path-segment">
    <xsl:param name="uri" />

    <xsl:choose>
      <xsl:when test="contains($uri, '/')">
        <xsl:call-template name="uri-last-path-segment">
          <xsl:with-param name="uri" select="substring-after($uri, '/')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$uri" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:transform>
