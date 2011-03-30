import sbt._
import de.element34.sbteclipsify._

class VisualizationDatasourceProject(info: ProjectInfo) extends DefaultProject(info)
with IdeaProject
with Eclipsify
//with posterous.Publish
{
  override def compileOptions = Seq(Deprecation, Unchecked)
  override def javaCompileOptions = javaCompileOptions("-Xlint:deprecation")//, "-Xlint:unchecked")
  override def filterScalaJars = false

  override def unmanagedClasspath = 
    super.unmanagedClasspath --- buildLibraryJar --- buildCompilerJar

  val scalaToolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

  //Dependencies
  //val scalap = "org.scala-lang" % "scalap" % buildScalaVersion withSources()
  val scalac = "org.scala-lang" % "scala-compiler" % buildScalaVersion withSources()
  val scalalib = "org.scala-lang" % "scala-library" % buildScalaVersion withSources()
  val unit = "junit" % "junit" % "4.8.2" % "test" withSources()
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.8-SNAPSHOT" withSources()
  val logback = "ch.qos.logback" % "logback-classic" % "0.9.25" withSources()
  //val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % "0.3.2"
  
  // Publishing
//  override def managedStyle = ManagedStyle.Maven
//  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
//  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
//  override def publishAction = super.publishAction && publishCurrentNotes
//  override def extraTags = "scalaj" :: super.extraTags

}
