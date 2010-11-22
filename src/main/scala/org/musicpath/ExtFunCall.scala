package org.musicpath

import net.sf.saxon.lib.ExtensionFunctionCall
import net.sf.saxon.om.SequenceIterator
import net.sf.saxon.tree.iter.SingletonIterator.makeIterator
//import net.sf.saxon.om.SingleNodeIterator
import net.sf.saxon.expr.XPathContext
import net.sf.saxon.om.StructuredQName
import net.sf.saxon.value.SequenceType.{SINGLE_STRING, SINGLE_ELEMENT_NODE}
import net.sf.saxon.value.StringValue
import javax.xml.transform.stream.StreamSource
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, StringReader}

import com.hp.hpl.jena.query.{
  Query, QueryExecution, QueryExecutionFactory, QueryFactory, QueryParseException, ResultSetFormatter, Syntax
}

object ExtFunCall extends ExtensionFunctionCall {
    def call(args:Array[SequenceIterator], context:XPathContext) = {
        val inputString =  args(0).next.getStringValue 
        val results = (QueryExecutionFactory.create(QueryFactory.create(inputString), Model.jModel)).execSelect()
        val outStream = new ByteArrayOutputStream
        ResultSetFormatter.outputAsXML(outStream, results)
        val inStream = new ByteArrayInputStream(outStream.toByteArray)
        makeIterator( context.getConfiguration.buildDocument(new StreamSource(inStream)) )
    }
}

// vim: set ts=4 sw=4 et:
