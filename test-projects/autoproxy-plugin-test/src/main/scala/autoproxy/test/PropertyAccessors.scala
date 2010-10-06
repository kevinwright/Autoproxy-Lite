package autoproxy.test
import autoproxy.annotation.proxy

//@ compiler-debug-print-txt-after-generatesynthetics
//@ compiler-debug-print-ast-after-generatesynthetics
//@ compiler-debug-browse-ast-after-generatesynthetics
//@ compiler-debug-print-txt-after-errorretyper
//@ compiler-debug-print-ast-after-errorretyper
//@ compiler-debug-browse-ast-after-errorretyper

class PropertyAccessorsProps {
  val s : String = "hello world"
  var i : Int = 0
}

class PropertyAccessors {	
  
  @proxy val props = new PropertyAccessorsProps
  def i2 = props.i 
  def i2_=(x$1: Int) = props.i = x$1

//  def s = props.s 
//  def i = props.i 
//  def i_=(n: Int) = props.i = n
  
  def s2 = props.s 
//  @proxy val foo = new Foo
    
}
