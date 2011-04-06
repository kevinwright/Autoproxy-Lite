package autoproxy.test

import autoproxy.annotation.proxy

trait Bippy {
	def bippy(i : Int) : String
}

class BippyTest(@proxy var dg : Bippy) extends Bippy