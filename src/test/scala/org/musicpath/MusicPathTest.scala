package org.musicpath

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.matchers.ShouldMatchers
//import org.specs._

class MusicPathTest extends ScalatraSuite with ShouldMatchers {
  addServlet(classOf[MusicPath], "/*")

  test("GET / should return index") {
    get("/") {
      status should equal (200)
      body should include ("Home")
    }
  }
  test("GET /bands should have something about Anon Remora having Ayla on guitar") {
    get("/bands") {
      status should equal (200)
      body should include ("Anon Remora")
      body should include ("Ayla")
      body should include ("Guitar")
    }
  }
  test("GET /people should have something about Ayla playing guitar in Anon Remora") {
    get("/people") {
      status should equal (200)
      body should include ("Ayla")
      body should include ("Guitar")
      body should include ("Anon Remora")
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
