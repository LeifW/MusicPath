package org.musicpath

import scala.xml.{NodeSeq,Text}
import java.net.URI
import Scene._
import net.croz.scardf._

object View {
  
  implicit def convert(uri:String):URI = new URI(uri)

  def band(band:Res) = 
        <band ref={band.uri.getPath}>
          <name>{band/Foaf.name}</name>
          <members>{
            for (stint <- band/staffed) yield
            <member>{stint/by/Foaf.givenname
            }</member>
          }</members>
        </band>

  def person(person:Res) =
    <person ref={person.uri.getPath}>
      <name>{person/Foaf.givenname}</name>
      <plays>{
        for (stint <- person/performs) yield
        <stint><in ref={(stint/in/asRes).uri.getPath}>{stint/in/Foaf.name}</in><instrument>(stint/plays/asRes).uri</instrument></stint>
      }</plays>
    </person>
}
