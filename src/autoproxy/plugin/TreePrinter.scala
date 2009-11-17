package autoproxy.plugin

import scala.tools._
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class TreePrinter(val global : Global, after: String) extends PluginComponent {
	import global._
    val phaseName = "treeprinter-" + after
    val runsAfter = List[String](after)
    override val runsRightAfter = Some(after)

    def globalDecls = global.definitions.RootClass.info.decls
 
    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
      def apply(unit: CompilationUnit) {
    	if (unit.source.path.contains("Bar")) {
    	  println("====================")
    	  println("after " + after + ": " + unit.source.path)
    	  //println("scope: " + globalDecls.toList.mkString);
    	  println("--------------------")
          println(unit.body)
    	  println("--------------------")
          println(nodePrinters nodeToString unit.body)
    	  println("====================")
    	}
      }
    }

}
