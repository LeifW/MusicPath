package com.portlanddatasystems.musicmap

import com.thinkminimo.step._
import SceneVocab._
import net.croz.scardf._

class MusicMap extends Step {

implicit val members = new Model

//  before {
//    contentType = "text/html"
//  }

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
    <h1>Hello!</h1>
    Please make a selection:
    <div><a href="/bands">bands</a></div>
    <div><a href="/people">people</a></div>
    </span>
  }
}
