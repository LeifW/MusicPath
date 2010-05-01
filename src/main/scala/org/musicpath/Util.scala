package org.musicpath
import com.hp.hpl.jena.query.{
  Query, QueryExecution, QueryExecutionFactory, QueryFactory, QueryParseException 
}
import com.hp.hpl.jena.query.Syntax
import net.croz.scardf.Model

object Util {
val bands2nodes = 
"""PREFIX : <http://musicpath.org/scene#> 
PREFIX band: <http://musicpath.org/bands/> 
PREFIX person: <http://musicpath.org/people/> 
PREFIX foaf: <http://xmlns.com/foaf/0.1/> 

CONSTRUCT {?b1 ?p ?b2} 
WHERE { ?p :performs [:in ?b1] . 
        ?p :performs [:in ?b2] .
}"""
def graph(implicit model: Model) = (QueryExecutionFactory.create( QueryFactory.create(bands2nodes, Syntax.syntaxARQ), model )).execConstruct()

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
