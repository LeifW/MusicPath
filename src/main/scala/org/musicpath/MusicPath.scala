package org.musicpath

import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._
import Scene._
import net.croz.scardf._

class MusicPath extends Step with UrlSupport {
  
  val url = "http://example.org/"
  implicit val model = new Model
 
  // Helper functions:

  // Put a <?xsl-stylesheet ?> processing-instruction at the top of the response.
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='edit.xsl'") ++ Text("\n") ++ content
  // Select all things of a given RDF:type.
  def allOf(category:Res) = Sparql selectAllX asRes where( (X, RDF.Type, category) ) from model
  
  def bandView(band:Res) = 
        <band ref={band.uri}>
          <name>{band/Foaf.name}</name>
          <members>{
            for (stint <- band/staffed) yield
            <member>{stint/by/Foaf.givenname
            }</member>
          }</members>
        </band>

  def personView(person:Res) =
    <person ref={person.uri}>
      <name>{person/Foaf.givenname}</name>
      <playsIn>{
        for (stint <- person/performs) yield
        <band>{stint/in/Foaf.name}<instrument>(stint/plays/asRes).uri</instrument></band>
      }</playsIn>
    </person>

  before {
    contentType = "application/xml"
  }
  override def init = this.model.read("file:///home/leif/band.ttl", "TURTLE")

  // Display all the bands in the system.
  get("/bands") {
    <bands>{
//      for (band <- allOf(Mo.MusicGroup) ) yield 
        allOf(Mo.MusicGroup) map bandView 
    }</bands>
  }

  get("/bands/:band") {
    bandView(Res(url+"bands/"+params(":band")))
  }

  // Display all the people in the system.
  get("/people") {
    <people>{ allOf(Foaf.Person) map personView }</people>
  }

  get("/people/:person") {
    personView(Res(url+"people/"+params(":person")))
  }

  get("/") {
    template(<span>
    <h1>Hello!</h1>
    Please make a selection: {request.getContextPath}
    <div><a href={url("/bands")}>bands</a></div>
    <div><a href={url("/people")}>people</a></div>
    </span>)
  }

  protected def contextPath = request.getContextPath
}
