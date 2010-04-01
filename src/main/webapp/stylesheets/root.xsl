<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
  <xsl:output method="xml" omit-xml-declaration="yes" doctype-public="-//W3C//DTD XHTML 1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" media-type="text/html" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>MusicPath: <xsl:value-of select="*/@title"/>
        </title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="people">
    <ul>
      <xsl:apply-templates select="person"/>
    </ul>
  </xsl:template>

  <xsl:template match="person">
    <li>
      <h4>
        <a href="{@ref}">
          <xsl:value-of select="name"/>
        </a>
      </h4>
Plays:<xsl:apply-templates select="plays/stint"/>
    </li>
  </xsl:template>

  <xsl:template match="stint">
    <xsl:value-of select="instrument"/>
 in <xsl:apply-templates select="in"/>
  </xsl:template>

  <xsl:template match="bands">
    <ul>
      <xsl:apply-templates select="band"/>
    </ul>
  </xsl:template>

  <xsl:template match="band">
    <li>
      <h4>
        <a href="{@ref}">
          <xsl:value-of select="name"/>
        </a>
        <xsl:text>: </xsl:text>
        <xsl:apply-templates select="members/member"/>
      </h4>
    </li>
  </xsl:template>

  <xsl:template match="member">
    <a href="{@ref}">
      <xsl:value-of select="name"/>
    </a>
    <xsl:if test="instr">(<xsl:apply-templates select="instr"/>
)</xsl:if>
    <xsl:if test="position()!=last()">, </xsl:if>
  </xsl:template>
  <xsl:template match="@*|xhtml:*">                       <!-- Copy XHTML through unscathed -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
