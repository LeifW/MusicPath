package org.musicpath

import org.specs._
import org.scardf._

/**
 * Created by IntelliJ IDEA.
 * User: leif
 * Date: 12/30/10
 * Time: 11:04 PM
 */

class RDFaTemplateTest extends Specification {
  "RDFa Templating" should {

    "generate fleshed-out HTML from an RDF graph" in {
      val List(leif, john, bill) = List("http://leif.com", "http://john.com", "http://bill.com").map( UriRef(_) )
      val graph = Graph(
        leif -(FOAF.name->"Leif", FOAF.knows-> ObjSet(bill, john)),
        john-FOAF.name->"John",
        bill-FOAF.name->"Bill")
      val template =
        <p xmlns:foaf="http://xmlns.com/foaf/0.1/">
          <span property="foaf:name"/>
          <a href="" rel="foaf:knows"><span property="foaf:name"/></a>
        </p>

      Template.processLinks(graph/leif)(template) must equalIgnoreSpace(
      <p xmlns:foaf="http://xmlns.com/foaf/0.1/">
        <span property="foaf:name">Leif</span>
        <a href="http://john.com" rel="foaf:knows"><span property="foaf:name">John</span></a>
        <a href="http://bill.com" rel="foaf:knows"><span property="foaf:name">Bill</span></a>
      </p>)
    }

  }

}