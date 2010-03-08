package com.portlanddatasystems.musicmap

import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._
import Scene._
import net.croz.scardf._

class MusicMap extends Step with UrlSupport {
  
  val url = "http://example.org/"
  implicit val model = new Model
 
  def allOf(category:Res) = Sparql selectAllX asRes where( (X, RDF.Type, category) ) from model
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='edit.xsl'") ++ Text("\n") ++ content

  before {
    contentType = "application/xml"
  }
  override def init = this.model.read("file:///home/leif/Projects/MusicMap/WebOutput/band.ttl", "TURTLE")

  get("/:type/:member") {
    <ul>
      <li>Type: {params(":type")}</li>
      <li>Member: {params(":member")}</li>
    </ul>
  }

  // Display all the bands in the system.
  get("/bands") {
    <bands>
      {for (band <- allOf(Mo.MusicGroup) ) yield <band>{band}</band>}
    </bands>
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
