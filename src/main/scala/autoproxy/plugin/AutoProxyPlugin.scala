package autoproxy.plugin

import scala.tools.nsc
import nsc.Global
import nsc.plugins.{Plugin, PluginComponent}

class AutoProxyPlugin(val global: Global) extends Plugin {
  import global._


  val AutoproxyAnnotationClass = "autoproxy.annotation.proxy"

  val name = "autoproxy"
  val description = "support for the @proxy annotation"

  val components = List[PluginComponent](
    new GenerateSynthetics(this, global)
  )

}
