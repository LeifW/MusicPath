package org.musicpath

import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._
import net.croz.scardf._
import Scene._

// This class mostly defines routes.  A couple view helpers are factored out into the "View" object.
class MusicPath extends Step {
  
  val url = "http://musicpath.org/"
  protected def contextPath = request.getContextPath
  implicit val model = new Model
 
  override def init = this.model.read("http://github.com/LeifW/MusicPath/raw/master/RDF/sample_data.ttl", "TURTLE")

  // Helper functions:

  // Put a <?xsl-stylesheet ?> processing-instruction at the top of the response.
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='/stylesheets/root.xsl'")++
                                          Text("\n")++
                                          content

  // Select all things of a given RDF:type.
  def allOf(category:Res) = Sparql selectAllX asRes where( (X, RDF.Type, category) ) from model
  
  before {
    contentType = "application/xml"
  }

  // Display all the bands in the system.
  get("/bands") { template(
    <bands title="Bands">{
        allOf(Mo.MusicGroup) map View.band
    }</bands>
  )}

  get("/bands/:band") { template(
    View band Res(url+"bands/"+params(":band"))
  )}

  // Display all the people in the system.
  get("/people") { template(
    <people title="People">{ allOf(Foaf.Person) map View.person }</people>
  )}

  get("/people/:person") { template(
    View person Res(url+"people/"+params(":person"))
  )}

  get("/") {
    template( 
    <span title="Home">
    <h1>Hello!</h1>
    Please make a selection: {request.getContextPath}
    <div><a href="/bands">bands</a></div>
    <div><a href="/people">people</a></div>
    </span>)
  }

}
