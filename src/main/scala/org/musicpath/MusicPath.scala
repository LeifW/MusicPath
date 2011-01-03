package org.musicpath

import java.io.{File, FileWriter, FileOutputStream, StringWriter, FileReader, BufferedReader}
import scala.xml.XML
import scala.xml.parsing.ConstructingParser.fromFile
import org.scalatra.ScalatraServlet                  // Web framework
import org.scardf._
import Template.processLinks

class MusicPath extends ScalatraServlet {
  val serializer = new Serializator(NTriple)
  val model = serializer.readFrom(new BufferedReader(new FileReader("dump.nt")))

  val root = XML.load("templates/index.html")
  val hostname = root\"@hostname"  // Overrides to the hostname in the URL's of resources can go in a <html hostname="http://foo.com/"> attribute.
  //def template(templ:String, resource:String) = processLinks( model/UriRef(hostname+resource) )( XML.load("templates/"+templ) )
  def template(templ:String, resource:String) = processLinks( model/UriRef(hostname+resource) )( fromFile(new File("templates/"+templ), true).document.docElem )

  get("/") {
    root
  }

/*
  get("/cull") {
    processLinks(Model/UriRef("http://musicpath.org/bands/cull"))(bandTemp)
  }
*/
  get("/:kind/") {
    val kind = params("kind")
    template(kind+"/index.html", kind+"/")
  }

  get("/:kind/:id") {
    val kind = params("kind")
    val id = params("id")
    template(kind+"/view.html", kind+"/"+id)
  }
}
