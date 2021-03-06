package org.musicpath

import scala.xml.{NodeSeq,Text}
import java.net.URI
import Scene._
import net.croz.scardf._

object View {
  
  //implicit def convert(uri:String):URI = new URI(uri)

  //def ref(thing:Res):String = thing.uri.getPath
  def ref(thing:Res):String = thing.jResource.getLocalName

  def instruments(stint:Node) = for (instr <- stint/plays) yield <instr ref={ref(instr asRes)}>{instr/(RDFS^"label")}</instr>
  //def toXml(collection:Node, name:String) = for (thing <- collection) yield Elem(

  def band(band:Res) = 
        <band ref={ref(band)}>
          <name>{band/FOAF.name}</name>
          <members>{
            for (stint <- band/position) yield
              <member ref={ref(stint/by/asRes)}>
                <name>{stint/by/FOAF.givenname}</name>
              {instruments(stint)}
              </member>
          }</members>
        </band>

  def person(person:Res) =
    <person ref={ref(person)}>
      <name>{person/FOAF.givenname}</name>
      <plays>{
        for (stint <- person/performs) yield
        <stint>
          <in ref={ref(stint/in/asRes)}>{stint/in/FOAF.name}</in>
          {instruments(stint)}
        </stint>
      }</plays>
    </person>
}
