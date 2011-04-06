package autoproxy.test

import autoproxy.annotation.proxy

trait TweedleDee {
  def x = "Hello World"
  def y = 42
}

class TweedleDum(@proxy dee: TweedleDee) extends TweedleDee {
  override def x = "I'm an override!"
}
