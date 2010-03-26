import sbt._

class MusicProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "provided"
  val step = "com.thinkminimo.step" %% "step" % "1.1.6-SNAPSHOT" 
  val scarla = "net.croz.scardf" % "scardf" % "0.2-SNAPSHOT"

  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  val jettytester = "org.mortbay.jetty" % "jetty-servlet-tester" % "6.1.22" % "test"
//  val specs = "org.scala-tools.testing" % "specs" % "1.6.2" % "test"
  val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
}

// vim: set ts=2 sw=2 et:
