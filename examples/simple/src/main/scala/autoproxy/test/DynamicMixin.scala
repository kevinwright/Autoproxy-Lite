package autoproxy.test

import autoproxy.annotation.proxy


trait DynamicMixin {
  def theString: String
  def theInt: Int
}

class DynamicMixinUser(@proxy instance: DynamicMixin) extends DynamicMixin {
  override def toString = "theString=" + theString + ", theInt=" + theInt
}