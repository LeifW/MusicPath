package org.musicpath
import net.croz.scardf.Vocabulary

object Scene extends Vocabulary("http://musicpath.org/scene#") {
  val Band = pRes("Band")
  val Stint = pRes("Stint")

  val by = pProp("by")
  val in = pProp("in")
  val name = pProp("name")
  val position = pProp("position")
  val performs = pProp("performs")
  val plays = pProp("plays")
  val started = pProp("started")
}

object Foaf extends Vocabulary("http://xmlns.com/foaf/0.1/") {
  val Person = pRes("Person")

  val givenname = pProp("givenname")
  val name = pProp("name")
}

object Mo extends Vocabulary("http://purl.org/ontology/mo/mit#") {
  val MusicGroup = pRes("MusicGroup")
  val Electric_Guitar = pRes("Electric_Guitar")
  val Electric_bass_guitar = pRes("Electric_bass_guitar")
  val Drumset = pRes("Drumset")
}

object Apf extends Vocabulary("http://jena.hpl.hp.com/ARQ/property#") {
  val splitIRI = pProp("splitIRI")
  val splitURI = pProp("splitURI")
}

/*
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
