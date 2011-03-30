package autoproxy.plugin

import org.specs.Specification
import tools.nsc.{CompilerCommand, Settings, Global, Main}
import tools.nsc.reporters.{ConsoleReporter, StoreReporter}
import tools.nsc.util.{ClassPath, FakePos}
import tools.nsc.plugins.Plugin

class PluginSpec extends Specification {
  "The plugin should" should {
    "compile a simple class" in {
      def scalacError(msg: String): Unit = println(msg + "\n  scalac -help  gives more information")

      val context     = Thread.currentThread.getContextClassLoader.asInstanceOf[java.net.URLClassLoader]
      val allcpurls      = context.getURLs.toList
      val cpurls = allcpurls filter {url => !url.toString.contains("project/boot") }
      val cpurlString = ClassPath.join(cpurls map (_.getPath) : _*)
      
      val testRoot = "src/test/resources/autoproxy/test/"
      val sources = List(
        "Main.scala",
        "PropertyAccessors.scala",
        "proxyAnnotation.scala") map {testRoot + _}

      val args = List(
        "-cp", cpurlString,
        "-verbose",
        //"-usejavacp",
        //"-nobootcp",
        "-Xplugin:test-projects/stub-jar/dynamic-mixin-stub.jar",
        "-Xplugin-require:autoproxy"
//        "-Xshow-phases"
//        "-Xplugin-list"
      ) ::: sources


      val ss       = new Settings(scalacError)
//      val reporter = new StoreReporter
      val reporter = new ConsoleReporter(ss)
      val command  = new CompilerCommand(args, ss)
      val settings = command.settings

      val compiler = new Global(settings, reporter)

//      new AutoProxyPlugin(compiler)
      
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