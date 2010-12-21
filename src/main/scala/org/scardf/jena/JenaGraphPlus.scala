package org.scardf.jena
import org.scardf.{Node, UriRef, RDF}
import com.hp.hpl.jena.rdf.model.Model

class JenaGraphPlus(m:Model) extends JenaGraph(m) {
    def resourcesWithProperty(p:UriRef, o:Node) = new JenaResIterator( m.listResourcesWithProperty(property(p), rdfnode(o)) )
    def instancesOf(o:UriRef) = resourcesWithProperty(RDF.Type, o)
}


// vim: set ts=4 sw=4 et:
