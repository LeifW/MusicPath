package org.musicpath

import net.sf.saxon.s9api._
object XQueryCall {
    val processor = new Processor(false)
    processor.registerExtensionFunction(ExtFunDef)
    val compiler = processor.newXQueryCompiler

    def run(queryString:String) = {
        val query = compiler.compile(queryString)
        val result = query.load.evaluate
        result.toString
    }
}


// vim: set ts=4 sw=4 et:
