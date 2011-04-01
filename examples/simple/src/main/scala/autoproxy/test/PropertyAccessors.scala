package autoproxy.test
//import autoproxy.annotation.proxy

class PropertyAccessorsProps {
  val s: String = "hello world"
  var i: Int = 0
}

class PropertyAccessors extends PropertyAccessorsProps {
  
  @proxy val props = new PropertyAccessorsProps
  def i2 = props.i
  def i2_=(x$1: Int) = props.i = x$1

//  def s = props.s 
//  def i = props.i 
//  def i_=(n: Int) = props.i = n
  
  def s2 = props.s 
//  @proxy val foo = new Foo
    
}
