package org.musicpath
import scala.xml.{ProcInstr,Text,NodeSeq}

object Edit {
    def root(title:String, content:NodeSeq):NodeSeq =
ProcInstr("xml-stylesheet", "type='text/xsl' href='/stylesheets/xsltforms/xsltforms.xsl'")++Text("\n")++
ProcInstr("xsltforms-options", "debug=\"no\"")++Text("\n")++
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:xf="http://www.w3.org/2002/xforms" xmlns:ev="http://www.w3.org/2001/xml-events" xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <head>
    <title>{title}</title>
    <link rel="stylesheet" src="/css/edit.css" type="text/css" />
    <!--style>
      @namespace xf url("http://www.w3.org/2002/xforms");
      body {{font-family: Ariel, Helvetica, san-serif}}
      /* Put a black border and background color around all specified XForms groups and gives them both margin and padding */
      xf|group {{border: solid black 1px; margin:15px 5px; padding:5px; background-color:Lavender;}}
      .group-label {{text-align:left;font-weight:bold;font-size:12pt;}}
      *:invalid {{ background-color: red; }}
    </style-->
    <xf:model>  
      <xf:instance id="default" src="xml"/>
      <xf:instance id="member">  <!-- Blank new Person -->
        <member ref="" xmlns={""}>
          <name/>
          <instr ref=""/>
        </member>
      </xf:instance>
      <xf:submission id="save" method="post" action="."/>
      <!--xf:bind -nodeset=members/member/@ref -type=xs:anyURI -->
    </xf:model>
  </head>

  <body>
  {content}
  </body>
</html>

  val band = root("Editing Band",
    <group nodeset="instance('default')" xmlns="http://www.w3.org/2002/xforms">
      <output value="name"/>

      <input ref="name">
        <label>Name</label>
      </input>
      <group>
        <label>Members</label>
        <repeat nodeset="members/member" appearance="compact" id="repeat">
          <group>
            <input ref="@ref">
              <label>Name</label>
            </input>
            <select1 ref="instr/@ref">              <label>Instrument</label>
              <item>
                <label>Guitar</label>
                <value>Electric_Guitar</value>
              </item>
              <item>
                <label>Bass</label>
                <value>Electric_bass_guitar</value>
              </item>
              <item>
                <label>Voice</label>
                <value>Voice</value>
              </item>
              <item>
                <label>Drums</label>
                <value>Drums</value>
              </item>
            </select1>
            <trigger>
              <label>X</label>
              <delete nodeset="." at="1" ev:event="DOMActivate"/>
            </trigger>
          </group>
        </repeat>
        <trigger>
          <label>New</label>
          <insert origin="instance('member')" nodeset="members/member" context="members" position="after" ev:event="DOMActivate"/>
        </trigger>
      </group>

      <submit submission="save">
        <label>Save</label>
      </submit>
    </group>
  )

  val person = root("Editing Person",
    <group xmlns="http://www.w3.org/2002/xforms">
      <output value="name"/>

      <input ref="name">
        <label>Name</label>
      </input>
      <group>
        <label>Bands</label>
        <repeat nodeset="plays/stint" appearance="compact" id="repeat">
          <group>
            <input ref="in/@ref">
              <label>Name</label>
            </input>
            <select1 ref="instr/@ref">              <label>Instrument</label>
              <item>
                <label>Guitar</label>
                <value>Electric_Guitar</value>
              </item>
              <item>
                <label>Bass</label>
                <value>Electric_bass_guitar</value>
              </item>
              <item>
                <label>Voice</label>
                <value>Voice</value>
              </item>
              <item>
                <label>Drums</label>
                <value>Drums</value>
              </item>
            </select1>
            <trigger>
              <label>X</label>
              <delete nodeset="." at="1" ev:event="DOMActivate"/>
            </trigger>
          </group>
        </repeat>
        <trigger>
          <label>New</label>
          <insert nodeset="plays/stint" at="index('repeat')" context="plays" position="after" ev:event="DOMActivate"/>
        </trigger>
      </group>

      <submit submission="save">
        <label>Save</label>
      </submit>
    </group>
  )

}

// vim: set ts=4 sw=4 et:
