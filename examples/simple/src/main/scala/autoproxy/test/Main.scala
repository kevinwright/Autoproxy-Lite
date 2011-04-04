package autoproxy.test
//@ compiler-debug-print-txt-after-parser
//@ compiler-debug-print-ast-after-parser
//@ compiler-debug-print-txt-after-earlynamer
//@ compiler-debug-print-txt-after-earlytyper
//@ compiler-debug-browse-ast-after-earlytyper

//@ compiler-debug-print-txt-after-generatesynthetics
//@ compiler-debug-print-ast-after-generatesynthetics
//@ compiler-debug-browse-ast-after-generatesynthetics
//@ compiler-debug-print-txt-after-errorretyper
//@ compiler-debug-print-ast-after-errorretyper
//@ compiler-debug-browse-ast-after-errorretyper

object Main {
	def main(args : Array[String]) {
		println("MAIN IS HERE!");
//		val bar = new Bar
//		
//		println(bar.prop1)
//		println(bar.method0)
		
		val pa = new PropertyAccessors
		
		
		pa.i2 = 42
		pa.i = 42
		println("i=" + pa.i)				
		println("i2=" + pa.i2)				
		println("s=" + pa.s)				
		println("s2=" + pa.s2)	

		println("ALL DONE");
	}
}

