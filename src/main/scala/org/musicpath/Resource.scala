package org.musicpath

import net.croz.scardf.Res                       // Jena wrapper
import scala.xml.Node

class Resource(val singular:String, val plural:String, val rdfType:Res, val view:(Res)=>Node) {

    def this(singular:String, rdfType:Res, view:(Res)=>Node) = this(singular, singular+"s", rdfType, view)
/*
    def viewAll

    def view
      if res/RDF.Type isEmpty
        edit

    def edit

    private def appearance
    */
}

// vim: set ts=4 sw=4 et:
