import sbt._
import de.element34.sbteclipsify._

class AutoproxyLitePluginProject(info: ProjectInfo) extends ParentProject(info)
with IdeaProject
with Eclipsify
//with posterous.Publish
{

  /////////////////
  // Repositories

  val scalaToolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

  
  /////////////////
  // Sub-projects
  
  lazy val thePlugin = project("plugin", "The plugin", new ThePlugin(_))
  lazy val examples = project("examples", "Examples", new Examples(_))

  class ThePlugin(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
  
    // General config
	
    override def compileOptions = Seq(Deprecation, Unchecked)
    override def javaCompileOptions = javaCompileOptions("-Xlint:deprecation")//, "-Xlint:unchecked")
    override def filterScalaJars = false

    override def unmanagedClasspath = 
      super.unmanagedClasspath --- buildLibraryJar --- buildCompilerJar

    //Dependencies
	
    val scalac = "org.scala-lang" % "scala-compiler" % buildScalaVersion withSources()
    val scalalib = "org.scala-lang" % "scala-library" % buildScalaVersion withSources()
    val unit = "junit" % "junit" % "4.8.2" % "test" withSources()
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.8-SNAPSHOT" withSources()
    val logback = "ch.qos.logback" % "logback-classic" % "0.9.25" withSources()
  }
  
  class Examples(info: ProjectInfo) extends ParentProject(info) with IdeaProject{
    lazy val simple = project("simple", "Simple Example", new ExampleProject(_), thePlugin)

    class ExampleProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins with IdeaProject {
      override def compileOptions = super.compileOptions ++ Seq(
        CompileOption("-Xplugin:plugin/src/test/stub-jar/dynamic-mixin-stub.jar"),
        CompileOption("-Xplugin-require:autoproxyxxx"),
        CompileOption("-Xprint:generatesynthetics")
      )

    }
  }

  ///////////////
  // Publishing
  
//  override def managedStyle = ManagedStyle.Maven
//  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
//  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
//  override def publishAction = super.publishAction && publishCurrentNotes
//  override def extraTags = "scalaj" :: super.extraTags

}
