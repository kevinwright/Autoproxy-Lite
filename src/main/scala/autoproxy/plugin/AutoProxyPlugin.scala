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

import scala.collection.mutable.MutableList

class AutoProxyPlugin(val global: Global) extends Plugin {
  import global._
  
  val AutoproxyAnnotationClass = "autoproxy.annotation.proxy"
  
  val name = "autoproxy"
  val description = "support for the @proxy annotation"
  
  val unitsWithSynthetics = new MutableList[CompilationUnit]
  val unitsInError = new MutableList[CompilationUnit]

  object earlyNamer extends PluginComponent {
	val global : AutoProxyPlugin.this.global.type = AutoProxyPlugin.this.global
    val phaseName = "earlynamer"
    val runsAfter = List[String]("parser")
    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
      override val checkable = false
      def apply(unit: CompilationUnit) {
    	val silentReporter = new SilentReporter
    	val cachedReporter = global.reporter
    	global.reporter = silentReporter
    	
    	try {
	      import analyzer.{newNamer, rootContext}
	      newNamer(rootContext(unit)).enterSym(unit.body)
    	} finally {
          global.reporter = cachedReporter
          if (silentReporter.errorReported) {
        	unitsInError += unit
          }
    	}
      }
    }
  }
  
  object earlyTyper extends PluginComponent {
	val global : AutoProxyPlugin.this.global.type = AutoProxyPlugin.this.global
    val phaseName = "earlytyper"
    val runsAfter = List[String]("earlynamer")
    override val runsRightAfter = Some("earlynamer")
    def newPhase(_prev: Phase): StdPhase = new StdPhase(_prev) {
      import analyzer.{resetTyper, newTyper, rootContext}
      resetTyper()
      override def run { 
        currentRun.units foreach applyPhase
      }
      def apply(unit: CompilationUnit) {
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
    }
  }
      
  val components = List[PluginComponent](
    new TreePrinter(global, "parser"),
    earlyNamer,
    earlyTyper,
    new TreePrinter(global, "earlytyper"),
    new GenerateSynthetics(this, global),
    new TreePrinter(global, "generatesynthetics"),
    new ErrorRetyper(this, global),
    new TreePrinter(global, "errorretyper")
  )
  
}
