package org.musicpath

import java.io.{File, FileWriter, FileOutputStream, StringWriter}
import org.apache.commons.io.IOUtils
import scala.io.Source
import scala.xml.XML
import scala.xml.{ProcInstr,NodeSeq,Text}
import org.scalatra.ScalatraServlet                  // Web framework
import org.scardf._
//import com.hp.hpl.jena.rdf.model.ResourceFactory  // 
//import com.tristanhunt._

// This class mostly defines routes.  A couple view helpers are factored out into the "View" object.
class MusicPath extends ScalatraServlet {

  /*
  before {
    response.setHeader("MS-Author-via", "SPARQL")
  }
  */

  get("/") {
      <home title="Home"/>
  }
}
