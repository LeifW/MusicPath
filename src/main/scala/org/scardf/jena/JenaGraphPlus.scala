package org.scardf.jena
import org.scardf.{Node, UriRef, SubjectNode, RdfTriple, RDF}
import com.hp.hpl.jena.rdf.model.Model

class JenaGraphPlus(m:Model) extends JenaGraph(m) {
    def resourcesWithProperty(p:UriRef, o:Node) = new JenaResIterator( m.listResourcesWithProperty(property(p), rdfnode(o)) )
    def instancesOf(o:UriRef) = resourcesWithProperty(RDF.Type, o)

    def contains( s: SubjectNode, p:UriRef ) = m.contains( resource( s ), property( p ) )

    def remove( t: RdfTriple ) = m remove statement( t )
}


// vim: set ts=4 sw=4 et:
