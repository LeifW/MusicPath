package org.musicpath

import java.io.{File, FileWriter, FileOutputStream, StringWriter}
import org.apache.commons.io.IOUtils
import scala.io.Source
import scala.xml.XML
import scala.xml.{ProcInstr,NodeSeq,Text}
import org.scalatra.ScalatraServlet                  // Web framework
import org.scardf._
import Template.processLinks
//import com.tristanhunt._

class MusicPath extends ScalatraServlet {

  val server = "http://musicpath.org/"

  get("/") {
      <home title="Home"/>
  }

/*
  get("/cull") {
    processLinks(Model/UriRef("http://musicpath.org/bands/cull"))(bandTemp)
  }
*/
  get("/:kind/:id") {
    val kind = params("kind")
    val id = params("id")
    val template = XML.load(kind+"/view.html")
    processLinks(Model/UriRef(server+kind+"/"+id))(template)
  }
}
