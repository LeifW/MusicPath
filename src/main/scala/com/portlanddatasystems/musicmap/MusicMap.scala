package com.portlanddatasystems.musicmap

import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._
import Scene._
import net.croz.scardf._

class MusicMap extends Step with UrlSupport {
  
  val url = "http://example.org/"
  implicit val model = new Model
 
  // Helper functions:

  // Select all things of a given RDF:type.
  def allOf(category:Res) = Sparql selectAllX asRes where( (X, RDF.Type, category) ) from model

  // Put a <?xsl-stylesheet ?> processing-instruction at the top of the response.
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='edit.xsl'") ++ Text("\n") ++ content

  before {
    contentType = "application/xml"
  }
  override def init = this.model.read("file:///home/leif/band.ttl", "TURTLE")

  get("/:type/:member") {
    <span>{Res(url + params(":type") + "/" + params(":member"))/Foaf.givenname/asString}
    <ul>
      <li>Type: {params(":type")}</li>
      <li>Member: {params(":member")}</li>
    </ul>
  </span>
  }

  // Display all the bands in the system.
  get("/bands") {
    <bands>{
      for (band <- allOf(Mo.MusicGroup) ) yield 
        <band ref={band.uri}>
          <name>{band/Foaf.name}</name>
          <members>{
            for (member <- band/performer) yield
            <member>{
            }</member>
          }</members>
        </band>
    }</bands>
  }

  // Display all the people in the system.
  get("/people") {
    <people>
      {for (person <- allOf(Foaf.Person)) yield <person>{person}</person>}
    </people>
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
