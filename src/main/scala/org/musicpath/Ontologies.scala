package org.musicpath
import org.scardf.Vocabulary
import com.hp.hpl.jena.vocabulary.{OWL => jOWL}
import com.hp.hpl.jena.sparql.vocabulary.{FOAF => jFOAF}

object Scene extends Vocabulary("http://musicpath.org/scene#") {
  val Band = Scene\"Band"
  val Stint = Scene\"Stint"
  val by = prop("by")
  val in = prop("in")
  val name = propStr("name")
  val position = prop("position")
  val performs = prop("performs")
  val plays = prop("plays")
  val started = propInt("started")

  val description = propStr("description") 

  val count = propInt("count")
}

object FOAF extends Vocabulary( jFOAF.getURI ) {
  val Person = FOAF\"Person"
  val List(name, givenname) =
      List("name", "givenname") map propStr
  val knows = prop("knows")
}

object MO extends Vocabulary("http://purl.org/ontology/mo/") {
  val MusicGroup = MO\"MusicGroup"
  /*
  val Electric_Guitar = MO\"Electric_Guitar")
  val Electric_bass_guitar = pRes("Electric_bass_guitar")
  val Drumset = pRes("Drumset")
  */
}

object OWL extends Vocabulary(jOWL.getURI) {
  val Class = prop("Class")
  val sameAs = prop("sameAs")
}
