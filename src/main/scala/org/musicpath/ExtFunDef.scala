package org.musicpath

import net.sf.saxon.functions.ExtensionFunctionDefinition
import net.sf.saxon.om.StructuredQName
import net.sf.saxon.value.SequenceType
import SequenceType.{SINGLE_STRING, SINGLE_ELEMENT_NODE, OPTIONAL_DOCUMENT_NODE}

object ExtFunDef extends ExtensionFunctionDefinition {
    val getFunctionQName = new StructuredQName("mp", "http://musicpath.org/ns/xsparql-fn#", "sparql")
    val getMinimumNumberOfArguments = 1
    //val getMaximumNumberOfArguments = 1
    val getArgumentTypes = List(SINGLE_STRING).toArray
    def getResultType(suppliedArgumentTypes:Array[SequenceType]) = OPTIONAL_DOCUMENT_NODE
    def makeCallExpression = ExtFunCall
}

// vim: set ts=4 sw=4 et:
