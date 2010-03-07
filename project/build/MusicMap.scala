import sbt._

class StepProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "provided"
  val step = "com.thinkminimo.step" %% "step" % "1.1.6-SNAPSHOT" 
  val scarla = "net.croz.scardf" % "scardf" % "0.2-SNAPSHOT"
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
}

// vim: set ts=4 sw=4 et:
