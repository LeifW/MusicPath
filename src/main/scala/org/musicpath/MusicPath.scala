package org.musicpath

import java.io.StringWriter
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

  def ref(thing:Res):String = thing.jResource.getLocalName
  def instruments(stint:Node) = for (instr <- stint/plays) yield <instr ref={ref(instr asRes)}>{instr/(RDFS^"label")}</instr>

  val people = new Resource("person", "people", FOAF.Person, person=>
    <person ref={ref(person)}>
      <name>{person/FOAF.givenname}</name>
      <plays>{
        for (stint <- person/performs) yield
        <stint>
          <in ref={ref(stint/in/asRes)}>{stint/in/FOAF.name}</in>
          {instruments(stint)}
        </stint>
      }</plays>
    </person>)

  val bands = new Resource("band", MO.MusicGroup, band=> 
    <band ref={ref(band)}>
      <name>{band/FOAF.name}</name>
      <members>{
        for (stint <- band/position) yield
          <member ref={ref(stint/by/asRes)}>
            <name>{stint/by/FOAF.givenname}</name>
            {instruments(stint)}
          </member>
      }</members>
    </band>)

  //val resources = List(people, bands)

  for (res <- List(people)) {
    get("/"+res.plural+"/?")(<root title={res.plural}>{allOf(res.rdfType) map res.view}</root>)
    get("/"+res.plural+"/:id")(res view Res(res.plural+"/"+params(":id")))
  }



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
    model.read("http://musicpath.org/dump.ttl", "TURTLE")
    <message>Stuff Loaded!</message>
  }
  get("/load/:format/:url") {
    model read(params(":url"), params(":format").toUpperCase)
    params(":url") ++ " Loaded!"
  }

  get("/graph") {
    Util.graph(model) write response.getOutputStream
    ()
  }

  get("/dump.ttl") {
    contentType = "text/plain"
    model.write(response.getOutputStream, "TURTLE")
    ()
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

  get("/bands/:band/xml") { 
    View band Res("bands/"+params(":band"))
  }

  get("/bands/:band/edit/?") { 
    Edit band
  }

  get("/bands/new") { 
    redirect(params("ref")+"/edit")
  }

  post("/bands/:band/?") { 
    val post = XML.load(request.getInputStream)
    val band_ref = params(":band")
    val band = Res( "bands/"+band_ref ) a MO.MusicGroup state( FOAF.name -> (post\"name" text))
    for (member <- post\"members"\"member") {
      val member_ref = member\"@ref" text
      val stint = Res("stints/"+band_ref+"_"+member_ref) a Stint state( by -> Res("people/"+member_ref) )
      for (instr <- member\"instr") 
        plays(stint) = Res("instruments/"+(member\"instr"\"@ref" text))
      println(Res("instruments/"+member\"instr"\"@ref"))
      position(band) = stint
    }
    <result>Okey-doke!</result>
    redirect("?created=true")
  }

  /* -- People URL's -- */

  // Display all the people in the system.
 /* get("/people/?") { template(
    <people title="People">{ allOf(FOAF.Person) map View.person }</people>
  )}

  get("/people/:person") { template(
    View person Res("people/"+params(":person"))
  )}
  */

  get("/people/:person/xml") { 
    View person Res("people/"+params(":person"))
  }

  get("/people/:person/edit/?") { 
    Edit person
  }

  get("/people/new") { 
    redirect(params("ref")+"/edit")
  }

  post("/people/:person/?") { 
    val post = XML.load(request.getInputStream)
    val member_ref = params(":person")
    val person = Res( "people/"+member_ref ) a FOAF.Person state( FOAF.givenname -> (post\"name" text))
    for (stint <- post\"plays"\"stint") {
      val band_ref = stint\"in"\"@ref" text
      val membership = Res("stints/"+band_ref+"_"+member_ref) a Stint state( by -> Res("people/"+member_ref) )
      for (instr <- stint\"instr") 
        plays(membership) = Res("instruments/"+instr.text)
      plays(person) = membership
    }
    <result>Okey-doke!</result>
    redirect("?created=true")
  }


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
