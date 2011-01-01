package org.musicpath

import org.scardf.{NodeConverter, SubjectNode, Property, GraphNode, UriRef, TypedLiteral, PlainLiteral, XSD}
import NodeConverter.asGraphNode
import scala.xml.{NodeSeq, Node, Elem, Text, NamespaceBinding, MetaData, UnprefixedAttribute}
import java.net.URI

/*
  Welcome to Linked Data.
  You are at Node 0.
  Use "rel" attribute to traverse to a new subject (node).
  Use "property" to list a property of the current subject.
  Go!
 */
    
object Template {

def maybe[A,B](default:B, option:Option[A], f:A=>B) = option.map(f).getOrElse(default)

    def propertize(e:Elem)(subject:GraphNode):NodeSeq = {
        val processedChildren = e.child flatMap processLinks(subject)
        propOption("property", e) match { 
            case Some(predicate) => (subject/predicate).flatMap {obj => 
                val (text, dt) = datatype(obj)
                e.copy(attributes = datatypeAttr(dt, e.attributes), child = Text(text) +: processedChildren)
            }.toSeq
            case None => e.copy(child=processedChildren)
        }
    }

    // of type (AttributeName, Function:Property=>List[Subjects]) => Option[ List[Subjects] ]

    def propOption(attrName:String, e:Elem):Option[UriRef] = e.attribute(attrName) match {
        case None => None
        case Some(List()) => None // ignore attributes with blank values here.
        case Some(content) => Some( UriRef({val text = content.text
                                            if (text.contains(':'))
                                                resolve(content.text, e.scope) 
                                            else
                                                text
                                        }) )
    }

    def datatype(l:org.scardf.Node):(String, String) = l match {
        case TypedLiteral(string, XSD.string) => (string, "xs:string")
        case TypedLiteral(string, XSD.integer) => (string, "xs:integer")
        case PlainLiteral(string, _) => (string, "")
    }


    def datatypeAttr(datatype:String, attr:MetaData):MetaData = {
        if (datatype == "")
            attr
        else
            new UnprefixedAttribute("datatype", datatype, attr)
    }


    private def realizeLink(e:Elem, attr:String)(subject:GraphNode):Elem = {
        // TODO: The cast won't work if there's blank nodes.
        val link = new UnprefixedAttribute(attr, subject.node.asInstanceOf[UriRef].uri, e.attributes.remove(attr))
        // Templating of properties is done a bit different on elements with a resource link, thus is is handled directly here.
        // If the property has a value,, put it in there as a text node.  If not, delete the property attribute.
        // More than one value is a warning (put that property on a child element!).
        val processedChildren = e.child flatMap processLinks(subject)
        val (contents, attributes) = propOption("property", e) match {
            case Some(predicate) => subject/predicate map datatype match {
                case List() => (processedChildren, link.remove("property"))
                case List((text, dt)) => (Text(text) +: processedChildren, datatypeAttr(dt, link))
                case (text, dt)::_ => {
                  println("More than one "+predicate.uri+" found for "+subject.node.toString+" on element "+e.label+", ignoring rest.")
                  (Text(text) +: processedChildren, datatypeAttr(dt, link))
                }
          }
            case None=> (processedChildren, link)
        }
        e.copy(attributes = attributes, child = contents )
    }

    def resolve(qname:String, scope:NamespaceBinding):String = {
        val Array(prefix, local) = qname split ':'
        scope.getURI(prefix) + local
    }

    private def copyTilLink(node:Node)(subject:GraphNode):Node = node match {
      case e:Elem => {
        getLink(e) match {
          case Some(ref) => realizeLink(e, ref)(subject)
          case None => e.copy(child=e.child.map(copyTilLink(_)(subject)))
        }
      }
      case other=> other
    }

    private def getLink(e:Elem):Option[String] ={
      val atts = e.attributes.map(_.key).toSet
      //List("resource", "href", "src", "about").dropWhile(!atts.contains(_)).headOption
      List("resource", "href", "src", "about").dropWhile(!atts.contains(_)).headOption
    }

    def revs(subject:GraphNode, property:UriRef):Iterable[GraphNode] = {
        val graph = subject.graph
        //graph.resourcesWithProperty(property, subject.node).map(graph/_)  
        graph.triplesLike(SubjectNode, property, subject.node) map (graph/_.subj)
    }


    implicit def convert(t:Tuple2[String,Elem]):Option[UriRef] = propOption(t._1, t._2)

    // TODO: the old subject is valid until the children of the node with the link.
    // Make two behaviours for this: 
    // - One which additionally carries a new subject, doesn't look for rel attributes, and looks for a link attribute.
    // - The other, the default, is to just carry a subject, and be on the lookout for rel attributes.
    def processLinks(subject:GraphNode)(node:Node):NodeSeq = node match {
        case e:Elem => {
            val currentSubject = propOption("about", e) orElse propOption("src", e) map (subject.graph/_) getOrElse subject
            ("rel", e).map(currentSubject/_/asGraphNode.iterable) orElse ("rev", e).map(revs(currentSubject,_)) match {
            // By this point we should either have Some(List[New Subjects]) or None.
                case Some(newSubjects) => {
                    // Take the first link attribute name found:
                    //e.attributes.find("resource") orElse e.attributes.find("href") map (_.key) match {
                    Set("resource", "href") intersect e.attributes.map(_.key).toSet headOption match {
                    //e.attributes.find("resource") orElse e.attributes.find("href") map (_.key) match {
                        case Some(ref) => newSubjects.flatMap(realizeLink(e, ref)).toSeq
                        case None => e.copy( child = newSubjects.flatMap( copyTilLink( e.child.dropWhile(!_.isInstanceOf[Elem]).head ) ).toSeq) 
                    }
                }
                // TODO: Maybe there's a href or a resource attribute currently set on this element that makes a new subject for the kids?
                case None => propertize(e)(currentSubject)
            }
        }
        case other => other
        //case t:Text => t
    }
}

// vim: set ts=4 sw=4 et:
