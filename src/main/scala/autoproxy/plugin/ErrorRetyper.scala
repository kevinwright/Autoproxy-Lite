package autoproxy.plugin

import scala.tools._
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.InfoTransform
import nsc.transform.TypingTransformers
import nsc.symtab.Flags._
import nsc.util.Position
import nsc.ast.TreeDSL
import nsc.typechecker
import scala.annotation.tailrec

class ErrorRetyper(plugin: AutoProxyPlugin, val global : Global) extends PluginComponent
  with Transform
  with TypingTransformers
{
  import global._
  import definitions._
  	  
  val runsAfter = List[String]("generatesynthetics")
  override val runsBefore = List[String]("namer")
  val phaseName = "errorretyper"
  def newTransformer(unit: CompilationUnit) = new RetypingTransformer(unit)    

  object ErrorCleaner extends Traverser {
    override def traverse(tree: Tree) = {
      if (tree.tpe == ErrorType) tree.tpe = null;
      if (tree.symbol != null && tree.symbol.isErroneous) tree.symbol = NoSymbol
      super.traverse(tree)
    }
  } 
  
  class RetypingTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
	var cleaning:Boolean = false
	
    override def transform(tree: Tree) : Tree = {
      if (cleaning) {
    	log("cleaning: " + tree)
	 	tree.tpe = null;
        if (tree.symbol != null && tree.symbol.isErroneous) {
          tree.symbol = NoSymbol
          log("denamed: " + tree)
        }
        super.transform(tree)
	  } else {
        if (tree.tpe == ErrorType) {
          cleaning = true
    	  log("retyping: " + tree)
          val cleantree = transform(tree)
	 	  localTyper.context1.reportGeneralErrors = true
    	  log("cleaned: " + tree)

    	
	 	  val typedtree = localTyper.typed { cleantree }
    	  log("retyped: " + typedtree)
          log("retyped tree: " + global.nodePrinters.nodeToString(typedtree))
    	  cleaning = false
    	  typedtree
	    } else { super.transform(tree) }
	  }
	}    
  }
}

