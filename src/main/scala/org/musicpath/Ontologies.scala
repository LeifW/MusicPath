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
}

object FOAF extends Vocabulary("") {
  val Person = FOAF\jFOAF.Person.getURI 

  val givenname = propStr( jFOAF.givenname.getURI )
  val name = propStr( jFOAF.name.getURI )
}

object MO extends Vocabulary("http://purl.org/ontology/mo/") {
  val MusicGroup = MO\"MusicGroup"
  /*
  val Electric_Guitar = MO\"Electric_Guitar")
  val Electric_bass_guitar = pRes("Electric_bass_guitar")
  val Drumset = pRes("Drumset")
  */
}

object OWL extends Vocabulary("") {
  val sameAs = prop( jOWL.sameAs.getURI )
}

/*
object Apf extends Vocabulary("http://jena.hpl.hp.com/ARQ/property#") {
  val splitIRI = pProp("splitIRI")
  val splitURI = pProp("splitURI")
}

Sparql select ('p, 'local, 'prefix) where (
  ('p, performs, 'stint), 
  ('stint, in, 'band), 
  ('band, Apf.splitIRI, 'list), 
  ('list, RDF.first, 'prefix), 
  ('list, RDF.rest, 'second), 
  ('second, RDF.first, 'local), 
  ('second, RDF.rest, 'rest)
)
*/
