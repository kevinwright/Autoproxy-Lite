import sbt._
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val edinboroughUniRepo = "www2.ph.ed.ac.uk-releases" at "http://www2.ph.ed.ac.uk/maven2/"
  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  val thuntRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.6"
  val eclipse = "de.element34" % "sbt-eclipsify" % "0.7.0"
}
