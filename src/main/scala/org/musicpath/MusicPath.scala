package org.musicpath

import scala.xml.XML
import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._                  // Web framework
import net.croz.scardf._                       // Jena wrapper
import com.hp.hpl.jena.rdf.model.ModelFactory  // Inferencing
import com.hp.hpl.jena.ontology.OntModelSpec   // Inferencing
import com.hp.hpl.jena.tdb.TDBFactory          // DB Store
import Scene._                                 // Predicates in musicpath ontology

// This class mostly defines routes.  A couple view helpers are factored out into the "View" object.
class MusicPath extends Step {

  // Blank string return value for null params
//  override protected def params = super.params withDefaultValue "" 
  
  implicit var model:Model = null
  val url = "http://musicpath.org/"

  override def init() {
    //super.init(config)
    val db = TDBFactory.createModel("tdb_store.db")
    model = new Model( ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, db) ) withPrefix url
  }

  override def destroy = model.close()

  def load {
    model read "RDF/sample_data.ttl"
  }

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

  get("/load") {
    model.read("http://github.com/LeifW/MusicPath/raw/master/RDF/schema.ttl", "TURTLE")
    model.read("http://github.com/LeifW/MusicPath/raw/master/RDF/sample_data.ttl", "TURTLE")
    <message>Stuff Loaded!</message>
  }
  get("/load/:format/:url") {
    model read(params(":url"), params(":format").toUpperCase)
    params(":url") ++ " Loaded!"
  }

  get("/dump") {
    contentType = "text/plain"
    model.dumped
  }

  // Display all the bands in the system.
  get("/bands/?") { template(
    <bands title="Bands">{ allOf(MO.MusicGroup) map View.band }</bands>
  )}

  get("/bands/:band/?") { 
    val res = Res("bands/"+params(":band"))
      if (res/RDF.Type isEmpty)
        redirect(params(":band")+"/edit")
      else
        template( View band res )
  }

  get("/bands/:band.xml") { 
    View band Res("bands/"+params(":band"))
  }

  get("/bands/:band/edit") { 
    Edit band params(":band")
  }

  get("/bands/new") { 
    redirect(params("ref")+"/edit")
  }

  post("/bands/:band/?") { 
    val post = XML.load(request.getInputStream)
    val band = Res( "bands/"+params(":band") ) a MO.MusicGroup state( FOAF.name -> (post\"name" text))
    for (member <- post\"members"\"member") {
      val stint = Anon( by -> Res("people/"+ (member\"@ref" text)) )
      for (instr <- member\"@instrument") 
        plays(stint) = Res("instruments/"+member\"@instrument")
      println(Res("instruments/"+member\"@instrument"))
      position(band) = stint
    }
    <result>Okey-doke!</result>
    redirect("?created=true")
  }

  /* -- People URL's -- */

  // Display all the people in the system.
  get("/people/?") { template(
    <people title="People">{ allOf(FOAF.Person) map View.person }</people>
  )}

  get("/people/:person") { template(
    View person Res("people/"+params(":person"))
  )}

  get("/") {
    template( 
    <div title="Home" xmlns="http://www.w3.org/1999/xhtml">
      <span id="tagline">"With God on our side, we will map out the bifurcations &amp; aglomerations of this cabal to the heart."</span>
    <h2>Welcome to the Cascadia Bureau of Band Statistics (B.B.S.)</h2>
    Please make a selection: 
    <div><a href="/bands">bands</a></div>
    <div><a href="/people">people</a></div>
    </div>)
  }

}
