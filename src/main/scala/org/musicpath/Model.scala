package org.musicpath

import org.scardf.jena.JenaGraphPlus           // Jena wrapper
import org.scardf.SetGraph
import com.hp.hpl.jena.rdf.model.ModelFactory  
import com.hp.hpl.jena.ontology.OntModelSpec   // Inferencing
import com.hp.hpl.jena.tdb.TDBFactory          // DB Store

// Using Scardf's native N-Triple parser to load the model during testing.
//object Model extends JenaGraphPlus( ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, TDBFactory.createModel("tdb_store.db")) )


// vim: set ts=4 sw=4 et:
