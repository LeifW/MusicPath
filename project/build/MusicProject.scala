import sbt._

class MusicProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  val step = "com.thinkminimo.step" %% "step" % "1.1.6-SNAPSHOT" 
  val scarla = "net.croz.scardf" %% "scardf" % "0.2-SNAPSHOT"
  val tdb = "com.hp.hpl.jena" % "tdb" % "0.8.5"

  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "provided"
  val jettytester = "org.mortbay.jetty" % "jetty-servlet-tester" % "6.1.22" % "test"

  val commons_io = "commons-io" % "commons-io" % "1.4"

  val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
  val knockoff = "com.tristanhunt" %% "knockoff" % "0.7.0-10"
  val t_repo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
}

// vim: set ts=2 sw=2 et:
