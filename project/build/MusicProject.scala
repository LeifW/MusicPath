import sbt._
import org.coffeescript.CoffeeScriptCompile

class MusicProject(info: ProjectInfo) extends DefaultWebProject(info) with CoffeeScriptCompile
{
  // Web server:
  //val scalatraVersion = "2.0.0-SNAPSHOT"
  val scalatraVersion = "2.0.0.M2"
  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  // RDF support:
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val scardf = "org.scardf" % "scardf" % "0.5-SNAPSHOT" // from "http://scardf.googlecode.com/files/scardf-0.5-SNAPSHOT.jar"
    val jodatime = "joda-time" % "joda-time" % "1.6.1" // Used by scardf
val jenaRepo = "OpenJena repo" at "http://openjena.org/repo"
//    val jena = "com.hp.hpl.jena" % "jena" % "2.6.3"
    val arq = "com.hp.hpl.jena" % "arq" % "2.8.7"
//    val jena = "com.hp.hpl.jena" % "jena" % "2.6.3"
  val tdb = "com.hp.hpl.jena" % "tdb" % "0.8.8"

//  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "provided"
//  val jettytester = "org.mortbay.jetty" % "jetty-servlet-tester" % "6.1.22" % "test"

  // Utility
  val commons_io = "commons-io" % "commons-io" % "1.4"

  val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"

  // Testing:
  val scalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val specs = "org.scala-tools.testing" % "specs_2.8.1_java_1.5" % "1.6.6" % "test"

  override def coffeeScriptDirectoryPathFinder = "src" / "main" / "coffeescript"
  override def coffeeScriptCompiledOuputDirectory = "src/main/webapp/js"
//  val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
//  val knockoff = "com.tristanhunt" %% "knockoff" % "0.7.2-13"
//  val t_repo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"

}

// vim: set ts=2 sw=2 et:
