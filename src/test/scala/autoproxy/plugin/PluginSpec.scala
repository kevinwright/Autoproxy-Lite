package autoproxy.plugin

import org.specs.Specification
import tools.nsc.{CompilerCommand, Settings, Global}
import tools.nsc.reporters.ConsoleReporter
import tools.nsc.util.ClassPath
import java.io.File

class PluginSpec extends Specification {
  "The plugin should" should {
    "compile a simple class" in {
      def scalacError(msg: String): Unit = println(msg + "\n  scalac -help  gives more information")

      val outputDir = "target/test-projects/simple"
      try {
        (new File(outputDir)).mkdirs
      }

      val context     = Thread.currentThread.getContextClassLoader.asInstanceOf[java.net.URLClassLoader]
      val allcpurls   = context.getURLs.toList
      val cpurls      = allcpurls filter {url => !url.toString.contains("project/boot") }
      val cpurlString = ClassPath.join(cpurls map (_.getPath) : _*)
      
      val testRoot = "src/test/projects/simple/autoproxy/test/"
      val sources = List(
        "Main.scala",
        "PropertyAccessors.scala",
        "proxyAnnotation.scala") map {testRoot + _}

      val args = List(
        "-cp", cpurlString,
        "-d", outputDir,
//        "-verbose",
        "-Xprint:generatesynthetics",
//        "-Yshow-trees",
//        "-Xshow-phases",
        "-Xplugin:src/test/stub-jar/dynamic-mixin-stub.jar",
        "-Xplugin-require:autoproxy"
      ) ::: sources

      val ss       = new Settings(scalacError)
//      val reporter = new StoreReporter
      val reporter = new ConsoleReporter(ss)
      val command  = new CompilerCommand(args, ss)
      val settings = command.settings

      val compiler = new Global(settings, reporter)


      if (reporter.hasErrors) {
//        reporter.infos foreach { println }
        reporter.flush
      }

      if (command.shouldStopWithInfo) {
        reporter.info(null, command.getInfoMessage(compiler), true)
      } else {
          val run = new compiler.Run()
          run compile command.files
//          reporter.infos foreach { println }
          reporter.flush

//          reporter.infos must haveSize(0)
      }

    }
  }
}