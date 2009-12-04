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
      //if (tree.symbol == null || tree.symbol == NoSymbol) tree.symbol = null
      super.traverse(tree)
    }
  } 

  object SymbolCleaner extends Traverser {
    override def traverse(tree: Tree) = {
      if (tree.symbol.isErroneous) {
 		tree.symbol = NoSymbol
      }	 	
      super.traverse(tree)
    }
  } 

  class RetypingTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
	val localNamer: analyzer.Namer = analyzer.newNamer(
      analyzer.rootContext(unit))
//      analyzer.rootContext(unit, EmptyTree, true))
    override def transform(tree: Tree) : Tree = {
      if (tree.tpe == ErrorType) {
    	log("retyping: " + tree)
    	//log("tree: " + global.nodePrinters.nodeToString(tree))
	 	ErrorCleaner.traverse(tree)
	 	localTyper.context1.reportGeneralErrors = true
	 	
    	log("cleaned: " + tree)

    	if (tree.symbol != null && tree.symbol.isErroneous) {
	 		SymbolCleaner.traverse(tree)
	 		//analyzer.newNamer(localTyper.context1).enterSym(tree)
	 		//localNamer.enterSym(tree)
            log("renamed: " + tree)
	 	}	 	
	 	
    	
	 	val typedtree = localTyper.typed { super.transform(tree) }
    	log("retyped: " + typedtree)
        log("tree: " + global.nodePrinters.nodeToString(typedtree))
    	typedtree
	  } else { super.transform(tree) }
	}    
  }
}

