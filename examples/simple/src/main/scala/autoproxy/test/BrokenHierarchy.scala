package autoproxy.test

import autoproxy.annotation.proxy


trait Top {
  def topMethod = 1
}

trait Mid extends Top {
  def midMethod = 2
}

class Bottom (@proxy mid: Mid) extends Mid with Top
//only midMethod should be proxied