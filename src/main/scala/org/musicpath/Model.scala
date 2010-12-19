package org.musicpath

import org.scardf.jena._                       // Jena wrapper
import com.hp.hpl.jena.rdf.model.ModelFactory  
import com.hp.hpl.jena.ontology.OntModelSpec   // Inferencing
import com.hp.hpl.jena.tdb.TDBFactory          // DB Store

object Model extends JNeo( ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, TDBFactory.createModel("tdb_store.db")) )


// vim: set ts=4 sw=4 et:
