package autoproxy.plugin

import scala.tools._
import nsc.Global
import nsc.plugins.PluginComponent
import nsc.transform.{Transform, TypingTransformers}
import nsc.symtab.Flags._
import nsc.ast.TreeDSL


class GenerateSynthetics(plugin: AutoProxyPlugin, val global: Global) extends PluginComponent
        with Transform
        with TypingTransformers
        with TreeDSL
{
  import global._
  //import definitions._

  import global.Tree
  
  val runsAfter = List[String]("typer")
  val phaseName = "generatesynthetics"

  def newTransformer(unit: CompilationUnit) = new AutoProxyTransformer(unit)

  class AutoProxyTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
    import CODE._

    private def cloneMethod(prototype: Symbol, owner: Symbol) = {
      val newSym = prototype.cloneSymbol(owner)
      newSym setFlag SYNTHETIC
      newSym resetFlag ABSTRACT
      newSym resetFlag DEFERRED
      if (prototype.isStable) (newSym setFlag STABLE)
      owner.info.decls enter newSym
    }

    private def cloneMethod2(prototype: Symbol, owner: Symbol) = {
      val methodName = prototype.name
      val flags = SYNTHETIC | (if (prototype.isStable) STABLE else 0)
      val method = owner.newMethod(NoPosition, methodName) setFlag flags
      method setInfo prototype.info
      owner.info.decls.enter(method).asInstanceOf[TermSymbol]
    }


    private def mkDelegate(owner: Symbol, tgtMember: Symbol, tgtMethod: Symbol, pos: Position) = {
      val delegate = cloneMethod(tgtMethod, owner)
      //val delegate = cloneMethod2(tgtMethod, owner)
      delegate setPos tgtMember.pos.focus

      log("owner = " + This(owner))

      val tgtGetter = if(tgtMember.hasGetter) tgtMember.getter(owner) else tgtMember
      log("target getter = " + tgtGetter)

      val selectTarget = This(owner) DOT tgtGetter DOT tgtMethod
      log("SelectTarget = " + nodeToString(selectTarget))

      val rhs: Tree =
        delegate.info match {
          case MethodType(params, _) => Apply(selectTarget, params.map(Ident(_)))
          case _ => selectTarget
        }

      //global.analyzer.UnTyper.traverse(rhs)
      log("rhs=" + nodeToString(rhs))

      val delegateDef = localTyper.typed {DEF(delegate) === rhs}

      log("delegate = " + nodeToString(delegateDef))

      delegateDef
    }

    private def publicMembersOf(sym: Symbol) =
      sym.tpe.members.filter(_.isPublic).filter(!_.isConstructor)

    private def publicMethodsOf(sym: Symbol) =
      publicMembersOf(sym).filter(_.isMethod)


    def generateDelegates(templ: Template, symbolToProxy: Symbol): List[Tree] = {
      val cls = symbolToProxy.owner //the class owning the symbol

      log("proxying symbol: " + symbolToProxy)
      log("owning class: " + cls)

      //need to find methods that are ONLY inherited from the type being proxied.
      //if available through any other route, then don't create the delegate.

      //first, locate all concrete public methods inherited from a superclass other than the proxy source
      val parents = cls.info.parents filter {symbolToProxy.tpe != }
      val nonProxyBaseClasses = parents.flatMap(_.baseClasses).distinct
      val nonProxyInheritedMethods = nonProxyBaseClasses.flatMap(publicMethodsOf).distinct
      val inheritedExclusions = nonProxyInheritedMethods.filterNot(_.isIncompleteIn(cls))
      log("inherited exclusions: " + inheritedExclusions.mkString(", "))

      // now locate all methods on the receiving class, and separate those which are inherited
      val definedMethods = publicMethodsOf(cls)
      val inheritedMethods = definedMethods.filter(_.owner != cls)
      val locallyDefinedMethods = definedMethods.filter(_.owner == cls).flatMap(_.allOverriddenSymbols)
      log("locally defined: " + locallyDefinedMethods.mkString(", "))

      //determine all methods that should be excluded from consideration for proxying
      val exclusions = inheritedExclusions ++ locallyDefinedMethods
      log("all exclusions: " + exclusions.mkString(", "))

      //now locate all methods available via the proxy source, and remove exclusions
      val candidates = publicMembersOf(symbolToProxy)
      log("candidates: " + candidates.mkString(", "))
      val requiredMethods = candidates filterNot (exclusions contains)
      log("required methods (candidates - exclusions): " + requiredMethods.mkString(", "))

      val needsOverride = requiredMethods.filterNot(_.isIncompleteIn(cls))
      log("needs override: " + needsOverride.mkString(", "))
      //val abstractMethods = definedMethods.filter(_.isIncompleteIn(cls))
      //val missingMethods =
      //  publicMembersOf(symbolToProxy).filter(mem => !definedMethods.contains(mem))



      val synthetics = requiredMethods map { mkDelegate(cls, symbolToProxy, _, symbolToProxy.pos.focus) }

      synthetics
    }

    override def transform(tree: Tree): Tree = {
      def isAccessor(tree: Tree) = tree match {
        case m: ValDef => true
        case _ => false
      }

      def shouldAutoProxySym(sym: Symbol) = {
        log("testing symbol: " + sym)
        if (sym != null) {
          val testSym = if (sym.isModule) sym.moduleClass else sym
          testSym.annotations foreach { ann => log("annotation:" + ann) }
          val gotOne = testSym.annotations exists {_.toString startsWith plugin.AutoproxyAnnotationClass}
          if(gotOne) log("got a live one here!")
          gotOne
        } else false
      }

      def shouldAutoProxy(tree: Tree) = {
        //isAccessor(tree) &&
        shouldAutoProxySym(tree.symbol)
      }

      val newTree = tree match {
        case ClassDef(mods, name, tparams, impl) =>
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