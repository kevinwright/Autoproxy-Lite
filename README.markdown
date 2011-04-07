Generates proxy methods, allowing mixin behaviour for dynamic instances.

Example
-------

        trait SomeClass {
          def theString: String
          def theInt: Int
        }

        class DynamicMixin(@proxy instance: SomeClass) extends SomeClass {
          override def toString = "theString=" + theString + ", theInt=" + theInt
          //synthesised:
          // def theString = instance.theString
          // def theInt = instance.theInt
        }

Note that DynamicMixin must also subclass SomeClass, to ensure that it will type-check correctly before the methods are generated.  This requirement will be dropped in the full autoproxy plugin.

Logic for deciding what methods to Proxy
----------------------------------------

