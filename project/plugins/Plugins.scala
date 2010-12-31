import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
//  val coffeeScriptSbtRepo = "coffeeScript sbt repo" at "http://repo.coderlukes.com"
// yes
  val coffeeScript = "org.coffeescript" % "coffee-script-sbt-plugin" % "0.9.3"
}

// vim: set ts=4 sw=4 et:
