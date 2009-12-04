package autoproxy.plugin

import scala.tools._
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

/**
 * For units that contain the string "@print-compiler-debug"
 * (usually in a comment)
 * Prints the AST after the specified phase 
 */
class TreePrinter(val global : Global, after: String) extends PluginComponent {
	import global._
    val phaseName = "treeprinter-" + after
    val runsAfter = List[String](after)
    override val runsRightAfter = Some(after)

    def globalDecls = global.definitions.RootClass.info.decls
 
    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
      def apply(unit: CompilationUnit) {
    	val printTxt = unit.comments.exists(_.text.contains("@compiler-debug-print-txt-after-" + after))
    	val printAst = unit.comments.exists(_.text.contains("@compiler-debug-print-ast-after-" + after))
    	val browseAst = unit.comments.exists(_.text.contains("@compiler-debug-browse-ast-after-" + after))
    	
    	if (printTxt || printAst) {
          println("====================")
    	  println("after " + after + ": " + unit.source.path)
    	  //println("scope: " + globalDecls.toList.mkString);
    	  if (printTxt) {
    	    println("--------------------")
            println(unit.body)
    	  }
          if (printAst) {
    	    println("--------------------")
            println(nodePrinters nodeToString unit.body)
          }
    	  println("====================")
    	}
    	
    	if (browseAst) {
    	  treeBrowser.browse(unit.body)
    	}
      }
    }

}
