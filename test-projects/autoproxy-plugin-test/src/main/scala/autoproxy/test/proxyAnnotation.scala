/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: $


package autoproxy.annotation

/** <p>
 *    When attached to a field, this annotation adds methods in the containing
 *    object that delegate to all publicly visible methods of the field's type.
 *    For example:
 *  </p><pre>
 *    case class Foo(x: String, y: String) {
 *      <b>def</b> concat = x + " - " + y
 *      <b>def</b> concat2 = x + ":" + y
 *    }
 *    
 *    class Bar {
 *      \@proxy <b>var</b> foo = new Foo("one", "two")</pre>
 *      <b>def</b> concat2 = x + "->" + y
 *    }
 *  <p>
 *    adds the following methods to the Foo class:
 *  </p><pre>
 *    <b>def</b> x = <b>foo</b>.x = s
 *    <b>def</b> x_=(s: String) = <b>foo</b>.x_=(s)
 *    <b>def</b> y = <b>foo</b>.y = s
 *    <b>def</b> y_=(s: String) = <b>foo</b>.y_=(s)
 *    <b>def</b> concat = <b>foo</b>.concat
 *  </pre>
 *  <p>
 *    Each delegate method will only be generated if it doesn't already exist
 *    (i.e. same name and signature) in the containing object.
 *  </p>
 */
class proxy extends StaticAnnotation

