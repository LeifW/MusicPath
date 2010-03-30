package org.musicpath

import scala.xml.{NodeSeq,Text}
import java.net.URI
import Scene._
import net.croz.scardf._

object View {
  
  implicit def convert(uri:String):URI = new URI(uri)

  def ref(thing:Res):String = thing.uri.getPath

  def instruments(stint:Node) = for (instr <- stint/plays) yield <instr>{instr}</instr>
  //def toXml(collection:Node, name:String) = for (thing <- collection) yield Elem(

  def band(band:Res) = 
        <band ref={ref(band)}>
          <name>{band/Foaf.name}</name>
          <members>{
            for (stint <- band/position) yield
              <member ref={ref(stint/by/asRes)}>
                <name>{stint/by/Foaf.givenname}</name>
              {instruments(stint)
              }</member>
          }</members>
        </band>

  def person(person:Res) =
    <person ref={ref(person)}>
      <name>{person/Foaf.givenname}</name>
      <plays>{
        for (stint <- person/performs) yield
        <stint>
          <in ref={ref(stint/in/asRes)}>{stint/in/Foaf.name}</in>
          {instruments(stint)}
        </stint>
      }</plays>
    </person>
}
