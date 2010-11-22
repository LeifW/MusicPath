package org.musicpath

import net.sf.saxon.s9api._
import java.io.File
import java.io.OutputStream
import java.net.URI

object XQueryCall {
    val processor = new Processor(false)
    processor.registerExtensionFunction(ExtFunDef)
    val compiler = processor.newXQueryCompiler

    def run(inputQuery:File, self:String) = {
        val query = compiler.compile(inputQuery)
        val evaluator = query.load
        //evaluator.setExternalVariable(new QName("this"), new XdmAtomicValue(new URI(self)))
        evaluator.setExternalVariable(new QName("this"), new XdmAtomicValue('<'+self+'>'))
        //val destination = new Serializer
        //destination.setOutputStream(ostream)
        //evaluator.setDestination(destination)
        //evaluator.run(destination)
        evaluator.evaluateSingle
    }
}


// vim: set ts=4 sw=4 et:
