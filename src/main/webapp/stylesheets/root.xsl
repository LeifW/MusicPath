<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
  <xsl:output method="xml" omit-xml-declaration="yes" doctype-public="-//W3C//DTD XHTML 1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" media-type="text/html" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>MusicPath: <xsl:value-of select="*/@title"/>
        </title>
        <link rel="alternate" type="application/x-wiki" title="Edit this page" href="{concat('/',name(*),'/',*/@ref,'/edit')}"/>
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
        <a href="{concat('/people/',@ref)}">
          <xsl:value-of select="name"/>
        </a>
      </h4>
Plays:<xsl:apply-templates select="plays/stint"/>
      <a href="{concat('/people/',@ref,'/edit')}">edit</a>
    </li>
  </xsl:template>
  <xsl:template match="stint">
    <xsl:value-of select="instrument"/>
 in <xsl:apply-templates select="in"/>
  </xsl:template>

  <xsl:template match="bands">
    <form action="/bands/new">New Band <br/>
      <label>URL</label>
      <input type="text" name="ref"/>
      <input type="submit" method="get"/>
    </form>
    <ul>
      <xsl:apply-templates select="band"/>
    </ul>
  </xsl:template>

  <xsl:template match="band">
    <li>
      <h4>
        <a href="{concat('/bands/',@ref)}">
          <xsl:value-of select="name"/>
        </a>
      </h4>
      <xsl:text>: </xsl:text>
      <xsl:apply-templates select="members/member"/>
      <span> <a href="{concat('/bands/',@ref,'/edit')}">edit</a>
      </span>
    </li>
  </xsl:template>
  <xsl:template match="member">
    <a href="{concat('/people/',@ref)}">
      <xsl:value-of select="name"/>
    </a>
    <xsl:if test="instr">(<xsl:apply-templates select="instr"/>
)</xsl:if>
    <xsl:if test="position()!=last()">, </xsl:if>
  </xsl:template>
  <xsl:template match="home">
    <div>
      <div>
        <a href="http://github.com/LeifW/MusicPath">Source</a>
 | <a href="http://wiki.pdxhub.org/pdx_music_map/">Brainstorming Wiki</a>
      </div>
      <span id="tagline">"With God on our side, we will map out the bifurcations &amp; agglomerations of this cabal to the heart."</span>
      <a href="/edit">edit</a>
      <h2>Welcome to the Cascadia Bureau of Band Statistics (B.B.S.)</h2>
Please make a selection:<div>
        <a href="/bands">bands</a>
      </div>
      <div>
        <a href="/people">people</a>
      </div>
    </div>
  </xsl:template>
  <xsl:template match="@*|xhtml:*">                       <!-- Copy XHTML through unscathed -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
