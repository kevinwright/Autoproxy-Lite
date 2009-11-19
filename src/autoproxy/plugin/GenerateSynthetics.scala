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


class GenerateSynthetics(plugin: AutoProxyPlugin, val global : Global) extends PluginComponent
  with Transform
  with TypingTransformers
  with TreeDSL
{
  import global._
  import definitions._
  	  
  val runsAfter = List[String]("earlytyper")
  val phaseName = "generatesynthetics"
  def newTransformer(unit: CompilationUnit) = new AutoProxyTransformer(unit)    

  class AutoProxyTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
    import CODE._

    private def mkDelegate(owner: Symbol, tgtMember: Symbol, tgtMethod: Symbol, pos: Position) = {
      val delegate = cloneMethod(tgtMethod, owner)
	        
      log("owner=" + This(owner))
	  
      val selectTarget = This(owner) DOT tgtMember DOT tgtMethod
      log("SelectTarget=")
      log(nodeToString(selectTarget))
	  
      val rhs : Tree =
        delegate.info match {
          case MethodType(params, _) => Apply(selectTarget, params.map(Ident(_)))
          case _ => selectTarget
        }
	    
      val delegateDef = localTyper.typed { DEF(delegate) === rhs } 
	    
      log(nodePrinters nodeToString delegateDef)
    
      delegateDef
    }
	
    private def publicMembersOf(sym:Symbol) =
      sym.tpe.members.filter(_.isPublic).filter(!_.isConstructor)
	
    private def publicMethodsOf(sym:Symbol) =
      publicMembersOf(sym).filter(_.isMethod)
  
    private def cloneMethod(prototype: Symbol, owner: Symbol) = {
      val newSym = prototype.cloneSymbol(owner)
      newSym setPos owner.pos.focus
      newSym setFlag SYNTHETICMETH
      owner.info.decls enter newSym    	
    }
      
    
    def generateDelegates(templ: Template, symbolToProxy: Symbol) : List[Tree] = {
      val cls = symbolToProxy.owner  //the class owning the symbol
    	
      log("proxying symbol: " + symbolToProxy)
      log("owning class: " + cls)
        
      val definedMethods = publicMembersOf(cls)
      val requiredMethods =
        publicMembersOf(symbolToProxy).filter(mem => !definedMethods.contains(mem))
    	
      log("defined methods: " + definedMethods.mkString(", "))
      log("missing methods: " + requiredMethods.mkString(", "))

      val synthetics = for (method <- requiredMethods) yield
        mkDelegate(cls, symbolToProxy, method, symbolToProxy.pos.focus)
      
      synthetics
    }
   
    override def transform(tree: Tree) : Tree = {
      def isAccessor(tree: Tree) = tree match {
    	case m:MemberDef if m.mods.isAccessor => true
    	case _ => false
      }
      
	  def shouldAutoProxySym(sym: Symbol) = {	 	
	 	if (sym != null) {
	 	  val testSym = if (sym.isModule) sym.moduleClass else sym
          testSym.annotations exists { _.toString == plugin.AutoproxyAnnotationClass }
	 	} else false
	  }

	  def shouldAutoProxy(tree: Tree) = {
	 	val nodeStr = nodePrinters.nodeToString(tree)
	 	!isAccessor(tree) && shouldAutoProxySym(tree.symbol)
	  }
		   
	  val newTree = tree match {
	    case ClassDef(mods,name,tparams,impl) =>
	      val delegs = for (member <- impl.body if shouldAutoProxy(member)) yield {
	     	log("found annotated member: " + member)
	        generateDelegates(impl, member.symbol)
	      }
	      val newImpl = treeCopy.Template(impl, impl.parents, impl.self, delegs.flatten ::: impl.body)
	      treeCopy.ClassDef(tree, mods, name, tparams, newImpl)
	    case _ => tree
	  }
	  super.transform(newTree)
	}    
  }
}