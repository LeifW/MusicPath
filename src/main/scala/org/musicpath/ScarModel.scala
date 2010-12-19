package org.scardf.jena
import com.hp.hpl.jena.rdf.model.{Model,RDFNode}
import org.scardf._

class JNeo(m:Model) extends JenaGraph(m) {
    //def thing = m.listStatements
    //def resourcesWithProperty(p:Property, o:RDFNode): Set[SubjectNode] = Set() ++ new JenaResIterator( m.listResourcesWithProperty(p:Property, o:RDFNode) )
    //def resourcesWithProperty[T](p:Property[T], o:Node) = new JenaResIterator( m.listResourcesWithProperty(p:Property, o:RDFNode) )
    def resourcesWithProperty[T](p:Property[T], o:Node) = new JenaResIterator( m.listResourcesWithProperty(property(p), rdfnode(o)) )
    def instancesOf(o:UriRef) = resourcesWithProperty(RDF.Type, o)
    // n.resourcesWithProperty(property(RDF.Type), rdfnode(Node from "socl"))
}


// vim: set ts=4 sw=4 et:
