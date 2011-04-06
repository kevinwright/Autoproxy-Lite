package autoproxy.test

import autoproxy.annotation.proxy
import annotation.target.field

trait Bippy {
	def bippy(i : Int) : String
}

class BippyTest(@proxy var dg : Bippy) extends Bippy