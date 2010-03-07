package com.portlanddatasystems.musicmap
import net.croz.scardf.Vocabulary

object SceneVocab extends Vocabulary("http://example.org/scene#") {
  val Band = pRes("Band")
  val Stint = pRes("Stint")

  val by = pProp("by")
  val in = pProp("in")
  val name = pProp("name")
  val performer = pProp("performer")
  val performs = pProp("performs")
  val plays = pProp("plays")
  val started = pProp("started")
}
