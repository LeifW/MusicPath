package org.musicpath

object Edit {
    def band(ref:String) =
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:xf="http://www.w3.org/2002/xforms" xmlns:ev="http://www.w3.org/2001/xml-events" xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <head>
    <title>Bands</title>
    <link rel="stylesheet" src="/css/edit.css" type="text/css" />
    <xf:model>
      <xf:instance src="."/>
      <xf:submission id="save" method="post" action="."/>
      <!--xf:bind -nodeset=members/member/@ref -type=xs:anyURI -->
    </xf:model>
  </head>

  <body>
    <group xmlns="http://www.w3.org/2002/xforms">
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
            <select1 ref="@instrument">              <label>Instrument</label>
              <item>
                <label>Guitar</label>
                <value>guitar</value>
              </item>
              <item>
                <label>Drums</label>
                <value>drums</value>
              </item>
            </select1>
            <trigger>
              <label>X</label>
              <delete nodeset="." at="1" if="count(//member) > 1" ev:event="DOMActivate"/>
            </trigger>
          </group>
        </repeat>
        <trigger>
          <label>New</label>
          <insert nodeset="members/member" at="index('repeat')" position="after" ev:event="DOMActivate"/>
        </trigger>
      </group>


      <submit submission="save">
        <label>Save</label>
      </submit>
    </group>
  </body>
</html>

}

// vim: set ts=4 sw=4 et:
