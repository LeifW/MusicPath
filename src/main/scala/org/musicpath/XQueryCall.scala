package org.musicpath

import net.sf.saxon.s9api._
import java.io.File

object XQueryCall {
    val processor = new Processor(false)
    processor.registerExtensionFunction(ExtFunDef)

    def run(inputQuery:File) = {
        val compiler = processor.newXQueryCompiler
        val query = compiler.compile(inputQuery)
        val result = query.load.evaluate
        result.toString
    }
}


// vim: set ts=4 sw=4 et:
