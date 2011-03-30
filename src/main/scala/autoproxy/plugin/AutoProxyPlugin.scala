package autoproxy.plugin

import scala.tools.nsc
import nsc.util._
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.TypingTransformers
import nsc.typechecker.Analyzer
import nsc.typechecker.Duplicators
import nsc.symtab.Flags._

import scala.collection.mutable.{MutableList, HashMap}

class AutoProxyPlugin(val global: Global) extends Plugin {
  import global._


  println("AutoProxy Plugin Loaded")
  val unitRollback = new HashMap[CompilationUnit, Tree]
//  val enteredSymbols = new HashMap[CompilationUnit, Seq[(Symbol, Scope)]]

//  object earlyAnalyzer extends {
//    val global: AutoProxyPlugin.this.global.type = AutoProxyPlugin.this.global
//  } with Analyzer {
//    private class EarlyNamer(context : Context) extends Namer(context) {
//      override def enterInScope(sym: Symbol, scope: Scope): Symbol = {
//        println("entering: " + sym + " in: " + scope)
//        enteredSymbols(context.unit) :+= (sym, scope)
//        super.enterInScope(sym, scope)
//      }
//    }
//
//    override def newNamer(context : Context) : Namer = new EarlyNamer(context)
//  }

  val AutoproxyAnnotationClass = "autoproxy.annotation.proxy"

  val name = "autoproxy"
  val description = "support for the @proxy annotation"

  val unitsWithSynthetics = new MutableList[CompilationUnit]
  val unitsInError = new MutableList[CompilationUnit]


  object earlyNamer extends PluginComponent {
    val global: AutoProxyPlugin.this.global.type = AutoProxyPlugin.this.global
    val phaseName = "earlynamer"
    val runsAfter = List[String]("parser")


    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
      override val checkable = false

      def apply(unit: CompilationUnit) {
        val silentReporter = new SilentReporter
        val cachedReporter = global.reporter
        global.reporter = silentReporter

        try {
//          import earlyAnalyzer.{newNamer, rootContext}
          import analyzer.{newNamer, rootContext}
          println("=== UNIT: " + unit + " ===")
//          enteredSymbols += unit -> Seq.empty
          unitRollback += unit -> unit.body
          newNamer(rootContext(unit)).enterSym(unit.body)
        } finally {
          global.reporter = cachedReporter
          if (silentReporter.errorReported) {
            unitsInError += unit
            resetAllAttrs(unit.body)
          }
        }
      }
    }
  }


  object earlyTyper extends PluginComponent {
    val global: AutoProxyPlugin.this.global.type = AutoProxyPlugin.this.global
    val phaseName = "earlytyper"
    val runsAfter = List[String]("earlynamer")
    override val runsRightAfter = Some("earlynamer")

    class UnlinkTransformer(unit: CompilationUnit) extends Transformer {
      val src = unit.source

      override def transform(t: global.Tree) : global.Tree = {
        val ret = super.transform(t)
        val sym = t.symbol
        if (sym != null && !sym.isPackage && sym.pos != NoPosition && sym.pos.source == src) {
          currentOwner.tpe.decls unlink sym
          println("unlinking [" + sym + "] owner [" + sym.owner + "] pos [" + sym.pos + "] from " + currentOwner.tpe.decls)
        }
        ret
      }
    }

    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
//      import earlyAnalyzer.{resetTyper, newTyper, rootContext}
      import analyzer.{resetTyper, newTyper, rootContext}
      resetTyper()
      override def run {
        currentRun.units foreach applyPhase
      }

      def apply(unit: CompilationUnit) {
        if (!unitsInError.contains(unit)) {
          val silentReporter = new SilentReporter
          val cachedReporter = global.reporter
          global.reporter = silentReporter

          try {
            unit.body = newTyper(rootContext(unit)).typed(unit.body)
            if (global.settings.Yrangepos.value && !global.reporter.hasErrors) global.validatePositions(unit.body)
            for (workItem <- unit.toCheck) workItem()
          } finally {
            unit.toCheck.clear()
            global.reporter = cachedReporter
            if (silentReporter.errorReported) {
              unitsInError += unit
            }
          }
        }
        if (unitsInError.contains(unit)) {
//          unit.body = unitRollback(unit)
          new UnlinkTransformer(unit).transform(unit.body)
          resetAllAttrs(unit.body)


//              resetAllAttrs(unit.body)

//              for((sym, scope) <- enteredSymbols(unit).reverse) {
//                if (!sym.isPackage) {
//                  println("unlinking: " + sym + " from: " + scope)
//                  scope unlink sym
//                } else {
//                  println("leaving: " + sym + " in: " + scope)
//                }
//              }
        }

      }
    }
  }

//  val components = List[PluginComponent](
//    new MyTreePrinter(global, "parser"),
//    earlyNamer,
//    earlyTyper,
//    new MyTreePrinter(global, "earlytyper"),
//    new GenerateSynthetics(this, global),
//    new MyTreePrinter(global, "generatesynthetics")
//    //new ErrorRetyper(this, global),
//    //new MyTreePrinter(global, "errorretyper")
//    )
  val components = List[PluginComponent](
    new GenerateSynthetics(this, global)
  )

}
