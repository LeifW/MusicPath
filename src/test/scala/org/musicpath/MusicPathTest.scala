package org.musicpath

import com.thinkminimo.step._
import org.scalatest.matchers.ShouldMatchers
import org.specs._

class MusicPathTest extends StepSuite with ShouldMatchers {
  route(classOf[MusicPath], "/*")

  test("GET / should return index") {
    get("/") {
      status should equal (200)
      body should include ("select")
    }
  }
}

/*
class MusicSpec extends Specification with StepTests {
  route(classOf[MusicPath], "/ *")

  "TheApp when using GET" should {
    "/ should return 'index'" in {
      get("/") {
        status mustEqual(200)
       // body mustEqual("hi!")
      }
    }
  }
}
*/
// vim: set ts=2 sw=2 et:
