package demangler

import com.indoorvivants.demangler.Demangler

object DemanglerSpec extends verify.BasicTestSuite {
  private def check(symbol: String, result: String) =
    assertEquals(Demangler.demangle(symbol), result)

  test("simple module defintion") {
    check("_SM7__constG1-0", "__const.<generated> -0")
  }

  test("method calls") {
    check(
      "_SM45scala.scalanative.regex.Parser$StringIteratorD4skipiuEO",
      "scala.scalanative.regex.Parser$StringIterator.skip(Int): Unit"
    )

    check(
      "_SM32scala.collection.IterableOnceOpsD6$init$uEO",
      "scala.collection.IterableOnceOps.$init$(): Unit"
    )

    check(
      "_SM22scala.runtime.Statics$D3mixiiiEO",
      "scala.runtime.Statics$.mix(Int, Int): Int"
    )
    check(
      "_SM34niocharset.UTF_8$DecodedMultiByte$D5applyccL33niocharset.UTF_8$DecodedMultiByteEO",
      "niocharset.UTF_8$DecodedMultiByte$.apply(Char, Char): niocharset.UTF_8$DecodedMultiByte"
    )
  }
}
