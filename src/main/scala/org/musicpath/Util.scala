package org.musicpath
import com.hp.hpl.jena.query.{
  Query, QueryExecution, QueryExecutionFactory, QueryFactory, QueryParseException 
}
import com.hp.hpl.jena.query.Syntax
import net.croz.scardf.Model

object Util {

  def loadDBTune( implicit model: Model ) {
    val qString = 
"PREFIX owl: <"+OWL.prefix+""">
PREFIX : <"""+Scene.prefix+""">
CONSTRUCT {?local ?prop ?value} 
WHERE {
  ?local owl:sameAs ?remote .
  ?local a ?type . 
  ?type :getDBTuneProps ?prop .
  SERVICE <http://dbtune.org/myspace> {
    ?remote ?prop ?value
  }
}"""
    (QueryExecutionFactory.create( QueryFactory.create(qString, Syntax.syntaxARQ), model )).execConstruct()
 }

}

// vim: set ts=4 sw=4 et:
