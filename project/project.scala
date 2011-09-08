import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.scala.incubator"
  val buildVersion      = "2.9.1"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    shellPrompt  := ShellPrompt.buildShellPrompt
  )
}

object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
  )

  val buildShellPrompt = { 
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, BuildSettings.buildVersion
      )
    }
  }
}

object Resolvers {
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

  val allResolvers = Seq(scalaToolsSnapshots)
}

//TODO: factor out dependencies into this class
/*
class Dependencies(scalaVersion: SettingKey[String]) {
  val logbackVer = "0.9.25"
  val specsVer = "1.6.8-SNAPSHOT"

  val scalacompiler = "org.scala-lang" % "scala-compiler" % scalaVersion
  val scalalib = "org.scala-lang" % "scala-library" % scalaVersion

  val logbackcore    = "ch.qos.logback" % "logback-core"     % logbackVer
  val logbackclassic = "ch.qos.logback" % "logback-classic"  % logbackVer

  val specs = "org.scala-tools.testing" %% "specs" % "1.6.8-SNAPSHOT"
}
*/

object PluginBuild extends Build {
  import Resolvers._
//  val deps = new Dependencies(scalaVersion)
//  import deps*._
  import BuildSettings._

  lazy val annotation = Project(
    "annotation",
    file("annotation"),
    settings = buildSettings )

  lazy val plugin = Project(
    "plugin",
    file("plugin"),
    settings = buildSettings ++ Seq(
      resolvers := allResolvers,
      libraryDependencies <++= scalaVersion { sv => Seq(
        "org.scala-lang" % "scala-compiler" % sv,
        "org.scala-lang" % "scala-library" % sv,
        "junit" % "junit" % "4.8.2" % "test",
        "org.scala-tools.testing" %% "specs" % "1.6.9-SNAPSHOT",
        "ch.qos.logback" % "logback-classic" % "0.9.25"
      )}
    )
  ) dependsOn (annotation)

  lazy val simpleExamples = Project(
    "simpleExamples",
    file("examples/simple"),
    settings = buildSettings ++ Seq(
      resolvers := allResolvers,
      libraryDependencies += "org.scala-tools.testing" %% "specs" % "1.6.9-SNAPSHOT",
      scalacOptions <+=
        (packagedArtifact in Compile in plugin in packageBin) map
        (jar => "-Xplugin:%s" format jar._2.getAbsolutePath),
      scalacOptions += "-Xplugin-require:autoproxy"
//        "-verbose",
//        "-usejavacp",
//        "-nobootcp",
//        "-Xplugin:plugin/src/test/stub-jar/dynamic-mixin-stub.jar",
//        "-Xprint:generatesynthetics",
//        "-Xprint:lazyvals",
//        "-Ylog:generatesynthetics",
//        "-Ylog:lambdalift",
//        "-Ydebug",
//        "-Yshow-syms"
//        "-Ycheck:generatesynthetics"
//        "-Ycheck:lazyvals"
//        "-Ybrowse:lazyvals"
//        "-Yshow-trees"
//        "-Xplugin-list"
//        "-Xshow-phases"        
    )
  ) dependsOn(annotation, plugin)
}


//TODO: Rewrite to use SBT 0.10 publishing

  ///////////////
  // Publishing
  
//  override def managedStyle = ManagedStyle.Maven
//  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
//  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
//  override def publishAction = super.publishAction && publishCurrentNotes
//  override def extraTags = "scalaj" :: super.extraTags

