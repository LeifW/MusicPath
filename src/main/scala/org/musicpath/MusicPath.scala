package org.musicpath

import java.io.{File, FileWriter, FileOutputStream, StringWriter}
import org.apache.commons.io.IOUtils
import scala.io.Source
import scala.xml.XML
import scala.xml.{ProcInstr,NodeSeq,Text}
import com.thinkminimo.step._                  // Web framework
import net.croz.scardf._                       // Jena wrapper
import com.hp.hpl.jena.rdf.model.ResourceFactory  // 
import saxon2scala.Saxon2Scala.convert
import Scene._                                 // Predicates in musicpath ontology
import net.sf.saxon.s9api.{Serializer,XdmNode}
//import com.tristanhunt._

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
    </person>,
    Edit.root("Editing Person",
    <group xmlns="http://www.w3.org/2002/xforms">
      <output value="name"/>

      <input ref="name">
        <label>Name</label>
      </input>
      <group>
        <label>Bands</label>
        <repeat nodeset="plays/stint" appearance="compact" id="repeat">
          <group>
            <input ref="in/@ref">
              <label>Name</label>
            </input>
            <select1 ref="instr/@ref">              <label>Instrument</label>
              <item>
                <label>Guitar</label>
                <value>Electric_Guitar</value>
              </item>
              <item>
                <label>Bass</label>
                <value>Electric_bass_guitar</value>
              </item>
              <item>
                <label>Voice</label>
                <value>Voice</value>
              </item>
              <item>
                <label>Drums</label>
                <value>Drums</value>
              </item>
            </select1>
            <trigger>
              <label>X</label>
              <delete nodeset="." at="1" ev:event="DOMActivate"/>
            </trigger>
          </group>
        </repeat>
        <trigger>
          <label>New</label>
          <insert nodeset="plays/stint" at="index('repeat')" context="plays" position="after" ev:event="DOMActivate"/>
        </trigger>
      </group>

      <submit submission="save">
        <label>Save</label>
      </submit>
    </group>)
  )

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
    </band>,
    Edit.root("Editing Band",
    <group nodeset="instance('default')" xmlns="http://www.w3.org/2002/xforms">
      <output value="name"/>

      <input ref="name">
        <label>Name</label>
      </input>
      <group>
        <label>Members</label>
        <repeat nodeset="members/member" appearance="compact" id="repeat">
          <group>
            <input ref="@ref">
              <label>Name</label>
            </input>
            <select1 ref="instr/@ref">              <label>Instrument</label>
              <item>
                <label>Guitar</label>
                <value>Electric_Guitar</value>
              </item>
              <item>
                <label>Bass</label>
                <value>Electric_bass_guitar</value>
              </item>
              <item>
                <label>Voice</label>
                <value>Voice</value>
              </item>
              <item>
                <label>Drums</label>
                <value>Drums</value>
              </item>
            </select1>
            <trigger>
              <label>X</label>
              <delete nodeset="." at="1" ev:event="DOMActivate"/>
            </trigger>
          </group>
        </repeat>
        <trigger>
          <label>New</label>
          <insert origin="instance('member')" nodeset="members/member" context="members" position="after" ev:event="DOMActivate"/>
        </trigger>
      </group>

      <submit submission="save">
        <label>Save</label>
      </submit>
    </group>)
  )

  // This loop adds all the standard REST CRUD Urls to each resource in the list.
  for (res <- List(people, bands)) {

    val subDir = "/"+res.plural  // Just to save repetitive typing.

    // GET /resourcetype
    // Display all of that resource type in the system, e.g. GET /people returns all FOAF.Person's.
    get(subDir+"/?") { 
      template( <root title={res.plural}>{allOf(res.rdfType) map res.view}</root> ) 
    }
    
    // GET /resourcetype/id
    // Display a singular record retrieved by id, e.g. GET /people/melissa returns the resource with id $HOSTNAME/people/melissa.
    // If the resource doesn't have a type, it's assumed to be a new resource, and redirects to the edit form.
    get(subDir+"/:id/?") {
      val req = Res(res.plural+"/"+params(":id"))
      if (req/RDF.Type isEmpty)
        redirect(params(":id")+"/edit")
      else {
        val templated = convert(XQueryCall.run(new File(res.plural, res.plural+".xquery"), baseUrl+res.plural+'/'+params(":id")))
        <html xmlns="http://www.w3.org/1999/xhtml"> 
          <head>
            <title>{templated\"@title"}</title>
          </head>
          <body>{templated}</body>
        </html>
      }
    }

    // GET /resourcetype/id/xml
    // This is just a straightforward view of the resource sans checks for the "edit new resource" to use.
    get(subDir+"/:id/xml") { 
      res view Res(res.plural+"/"+params(":id")) 
    }

    // GET /resourcetype/id/edit
    get(subDir+"/:id/edit/?") { 
      res edit                   // Yes, fire up an ole' Macintosh image!
    }  
  
    // GET /resourcetype/new
    get(subDir+"/new") { 
      redirect(params("ref")+"/edit") 
    }

    get(subDir+"/edit/?") {
      template(
     <form action="." method="post" xmlns="http://www.w3.org/1999/xhtml">
       <label>Edit some <a href="http://xsparql.deri.org/spec">XSPARQL</a></label>
        <textarea rows="40" cols="80" name="content">{ Source.fromFile(res.plural+"/"+res.plural+".xsparql").mkString }</textarea>
        <input type="submit" method="post"/>
      </form>
     )}

    post(subDir+"/?") {
      val filename = res.plural+"/"+res.plural
      val xsOut = new FileWriter(filename+".xsparql")
      xsOut.write( params("content") )
      xsOut.close()
      val proc = Runtime.getRuntime.exec("xsparqler/xsparqler.py "+filename+".xsparql")
      proc.waitFor
      if (proc.exitValue == 0) {
        val xqOut = new FileOutputStream(filename+".xquery")
        IOUtils.copy(proc.getInputStream, xqOut)
        xqOut.close()
        template(<p xmlns="http://www.w3.org/1999/xhtml">Yeah, you saved it</p>)
      } else {
        response.setStatus(400)
        contentType = "text/plain"
        IOUtils.copy(proc.getErrorStream, response.getOutputStream)
        ()
      }
    }

  }

  
  val baseUrl = "http://musicpath.org/"
  //val db = TDBFactory.createModel("tdb_store.db")
  //implicit val model:Model = new Model( ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, db) ) withPrefix url
  implicit val model = Model withPrefix baseUrl
  //model.read("file:///home/leif/musicpath/dump.ttl", "TURTLE")

  override def destroy {
    model.close()
  }

  // Helper functions:

  // Put a <?xsl-stylesheet ?> processing-instruction at the top of the response.
  def template(content:NodeSeq):NodeSeq = ProcInstr("xml-stylesheet", "type='text/xsl' href='/stylesheets/root.xsl'")++
                                          Text("\n")++
                                          content

  // Select all things of a given RDF:type.
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

  get("/graph.rdf") {
    Util.graph(model) write response.getOutputStream
    ()
  }

  get("/dump.ttl") {
    contentType = "text/plain"
    model.write(response.getOutputStream, "TURTLE")
    ()
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

  // POST home: Res("").jResource.getProperty(description).changeObject(ResourceFactory.createTypedLiteral("yes"))
  // Res("").jResource getProperty description changeObject ResourceFactory.createTypedLiteral("yes")
  post("/") {
    Res("").jResource.getProperty(description).changeObject(ResourceFactory.createTypedLiteral(params("content")))
  }

  get("/content/?") {
    template( 
      <div title="Home" xmlns="http://www.w3.org/1999/xhtml">
        {Res("")/description}
      </div>
    )
  }

  get("/edit/?") {
    template(
   <form action="/" method="post" xmlns="http://www.w3.org/1999/xhtml">
      <label>Home page content</label>
      <input type="text" name="content"/>
      <input type="submit" method="post"/>
    </form>
   )}


  get("/") {
    template( 
      <home title="Home"/>
    )
  }

  // XSPARQL templating:
  for (resource <- List("people", "bands")) {

  get("/template/"+resource+"/:id/?") {
    XQueryCall.run(new File(resource, resource+".xquery"), baseUrl+resource+'/'+params(":id"))
  }

  get("/template/"+resource+"/edit/?") {
    template(
   <form action="." method="post" xmlns="http://www.w3.org/1999/xhtml">
     <label>Edit some <a href="http://xsparql.deri.org/spec">XSPARQL</a></label>
      <textarea rows="40" cols="80" name="content">{ Source.fromFile(resource+"/"+resource+".xsparql").mkString }</textarea>
      <input type="submit" method="post"/>
    </form>
   )}

  post("/template/"+resource+"/?") {
    val filename = resource+"/"+resource
    val xsOut = new FileWriter(filename+".xsparql")
    xsOut.write( params("content") )
    xsOut.close()
    val proc = Runtime.getRuntime.exec("xsparqler/xsparqler.py "+filename+".xsparql")
    proc.waitFor
    if (proc.exitValue == 0) {
      val xqOut = new FileOutputStream(filename+".xquery")
      IOUtils.copy(proc.getInputStream, xqOut)
      xqOut.close()
      template(<p xmlns="http://www.w3.org/1999/xhtml">Yeah, you saved it</p>)
    } else {
      response.setStatus(400)
      contentType = "text/plain"
      IOUtils.copy(proc.getErrorStream, response.getOutputStream)
      ()
    }
  }
}

}
