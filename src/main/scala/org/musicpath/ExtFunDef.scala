package org.musicpath

import net.sf.saxon.lib.ExtensionFunctionDefinition
import net.sf.saxon.om.StructuredQName
import net.sf.saxon.value.SequenceType
import SequenceType.{SINGLE_STRING, SINGLE_ELEMENT_NODE, OPTIONAL_DOCUMENT_NODE}

object ExtFunDef extends ExtensionFunctionDefinition {
    val getFunctionQName = new StructuredQName("mp", "http://musicpath.org/ns/xsparql-fn#", "sparql")
    override val getMinimumNumberOfArguments = 1
    override val getMaximumNumberOfArguments = 1
    val getArgumentTypes = List(SINGLE_STRING).toArray
    def getResultType(suppliedArgumentTypes:Array[SequenceType]) = OPTIONAL_DOCUMENT_NODE
    val makeCallExpression = ExtFunCall
}

// vim: set ts=4 sw=4 et:
