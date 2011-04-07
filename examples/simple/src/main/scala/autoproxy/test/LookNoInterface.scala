package autoproxy.test
import autoproxy.annotation.proxy

// The method LookNoInterface.lniNum doesn't explicitly override Lni.lniNum,
// or share inheritance of this method via some common supertype.
// Nevertheless, it should still suppress synthetic generation
// through having the same signature.

trait Lni {
  def lniNum = 42
  def lniStr = "Hi Mum!"
}

class LookNoInterface(@proxy lni: Lni) {
  def lniNum = 69
}

