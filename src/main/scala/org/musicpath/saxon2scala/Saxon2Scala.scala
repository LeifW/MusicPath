package org.musicpath.saxon2scala

import net.sf.saxon.s9api._
import XdmNodeKind._
import scala.collection.jcl.MutableIterator.Wrapper
import scala.xml._
object Saxon2Scala {
    implicit def toScalaCollection(it:java.util.Iterator[XdmItem]) = new Wrapper(it)
    implicit def convert(node:XdmItem):Node = traverse(node, TopScope, Set())
    implicit def item2Node(i:XdmItem):XdmNode = i.asInstanceOf[XdmNode]
    protected def saxon2scalaAttr(scalaAtts:MetaData, saxonAtt:XdmItem) = {
        // not dealing withi namespaced attributed yet.
        new UnprefixedAttribute(saxonAtt.getNodeName.getLocalName, saxonAtt.getStringValue, scalaAtts)
    }
    protected def traverse(node:XdmNode, scope:NamespaceBinding, defaultNSs:Set[String]):Node = node.getNodeKind match {
        case TEXT => Text(node.getStringValue)
        case ELEMENT => val attributes = node.axisIterator(Axis.ATTRIBUTE).foldLeft(Null.asInstanceOf[MetaData])(saxon2scalaAttr)
                        val qname = node.getNodeName
                        val namespace = qname.getNamespaceURI
                        val prefix = qname.getPrefix
                        val (newScope, newDefaults) = 
                            if (namespace != "")
                                if (prefix == "")
                                    if (defaultNSs.contains(namespace)) 
                                        (scope, defaultNSs)
                                     else
                                        (new NamespaceBinding(null, namespace, scope), defaultNSs + namespace) 
                                else 
                                    if (scope.getPrefix(namespace) == prefix)
                                        (scope, defaultNSs) 
                                    else
                                        (new NamespaceBinding(prefix, namespace, scope), defaultNSs) 
                            else 
                                (scope, defaultNSs)
                        Elem(if (prefix == "") null else prefix, qname.getLocalName, attributes, newScope, node.axisIterator(Axis.CHILD).map((i)=>traverse(i, newScope, newDefaults)).toList: _*)
    }
}

// vim: set ts=4 sw=4 et:
