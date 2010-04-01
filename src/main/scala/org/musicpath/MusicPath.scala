package org.musicpath

import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._                  // Web framework
import net.croz.scardf._                       // Jena wrapper
import com.hp.hpl.jena.rdf.model.ModelFactory  // Inferencing
import com.hp.hpl.jena.ontology.OntModelSpec   // Inferencing
import com.hp.hpl.jena.tdb.TDBFactory          // DB Store
import Scene._                                 // Predicates in musicpath ontology

// This class mostly defines routes.  A couple view helpers are factored out into the "View" object.
class MusicPath extends Step {
  
  implicit var model:Model = null
  val url = "http://musicpath.org/"
  protected def contextPath = request.getContextPath

  override def init {
    val db = TDBFactory.createModel("tdb_store.db")
    model = new Model( ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, db) ) withPrefix url
  }

  override def destroy = model.close()

  // Helper functions:

  // Put a <?xsl-stylesheet ?> processing-instruction at the top of the response.
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='/stylesheets/root.xsl'")++
                                          Text("\n")++
                                          content

  // Select all things of a given RDF:type.
  //def allOf(category:Res) = Sparql selectAllX asRes where( (X, RDF.Type, category) ) from model
  def allOf(category:Res) = model.listRes(RDF.Type, category)
  
  before {
    contentType = "application/xml"
  }

  // Load the schema, and the initial sample data.
  get("/load") {
    model.read("http://github.com/LeifW/MusicPath/raw/master/RDF/schema.ttl", "TURTLE")
    model.read("http://github.com/LeifW/MusicPath/raw/master/RDF/sample_data.ttl", "TURTLE")
    "Stuff Loaded!"
  }
  get("/load/:format/:url") {
    model read(params(":url"), params(":format").toUpperCase)
    params(":url") ++ " Loaded!"
  }

  // Display all the bands in the system.
  get("/bands/?") { template(
    <bands title="Bands">{ allOf(MO.MusicGroup) map View.band }</bands>
  )}

  get("/bands/:band") { template(
    View band Res("bands/"+params(":band"))
  )}

  // Display all the people in the system.
  get("/people/?") { template(
    <people title="People">{ allOf(FOAF.Person) map View.person }</people>
  )}

  get("/people/:person") { template(
    View person Res("people/"+params(":person"))
  )}

  get("/") {
    template( 
    <span title="Home" xmlns="http://www.w3.org/1999/xhtml">
    <h1>Hello!</h1>
    Please make a selection: {request.getContextPath}
    <div><a href="/bands">bands</a></div>
    <div><a href="/people">people</a></div>
    </span>)
  }

}
