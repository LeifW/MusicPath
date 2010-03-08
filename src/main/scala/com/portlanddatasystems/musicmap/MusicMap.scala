package com.portlanddatasystems.musicmap

import scala.xml.ProcInstr
import com.thinkminimo.step._
import Scene._
import net.croz.scardf._

class MusicMap extends Step with UrlSupport {

implicit val model = new Model

//  before {
//    contentType = "text/html"
//  }
  override def init() = this.model.read("file:///home/leif/Projects/MusicMap/WebOutput/band.ttl", "TURTLE")

  get("/:type/:member") {
    <ul>
      <li>Type: {params(":type")}</li>
      <li>Member: {params(":member")}</li>
    </ul>
  }

  get("/:type") {
    <span>
      You want all the {params(":type")}'s
      { ((params(":type")).getClass()) }
      for (thing - store.type) yield <type> </type>
    </span>
  }

  get("/bands") {
    <bands>
      for (band &lt;- store.bands) yield <band>{}</band>
    </bands>
  }

  get("/form") {
    <form action='post' method='POST'>
      Post something: <input name='submission' type='text'/>
      <input type='submit'/>
    </form>
  }

  post("/post") {
    <h1>You posted: {params("submission")}</h1>
  }

  get("/") {
    <span>
    //ProcInstr("xml-stylesheet", "text/xsl")
    <h1>Hello!</h1>
    Please make a selection:
    <div><a href={url("/bands")}>bands</a></div>
    <div><a href={url("/people")}>people</a></div>
    </span>
  }

  protected def contextPath = request.getContextPath
}
