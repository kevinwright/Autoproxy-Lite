package autoproxy.test


import org.specs.Specification
import tools.nsc.{CompilerCommand, Settings, Global}
import tools.nsc.reporters.ConsoleReporter
import tools.nsc.util.ClassPath
import java.io.File

class DynamicMixinSpec extends Specification {
  "The dynamic mixin" should {
    "work" in {
      val theTrait = new DynamicMixin {
        val theString = "bippy"
        val theInt = 42
      }
      val user = new DynamicMixinUser(theTrait)

      user.theString mustEqual "bippy"
      user.theInt mustEqual 42
    }
  }
}