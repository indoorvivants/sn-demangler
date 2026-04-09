import sn_demangler.Demangler

class DemanglerSpec extends munit.FunSuite {
  private def check(symbol: String, result: String) =
    assertEquals(Demangler.demangle(symbol), result)

  private def checkAll(text: String, names: List[String]) = {
    val all = Demangler.demangleAll(text)
    assertEquals(all.map(_.name).toSet, names.toSet)

    all.foreach { d =>
      val slice = text.slice(d.start, d.end + 1)
      assertEquals(Demangler.demangle(slice), d.name)
    }
  }

  test("simple module defintion") {
    check("_SM7__constG1-0", "__const.<generated> -0")
  }

  test("in text") {
    // check("_SM7__constG1-0", "__const.<generated> -0")
    val text =
      """
    | _SM7__constG1-0
    | hello world _SM45scala.scalanative.regex.Parser$StringIteratorD4skipiuEO world
    | _SINCORRECT
    """.stripMargin.trim()

    // println(Demangler.demangleAll(text))
    checkAll(
      text,
      List(
        "__const.<generated> -0",
        "scala.scalanative.regex.Parser$StringIterator.skip(Int): Unit"
      )
    )

    checkAll("", Nil)
    checkAll("hello world", Nil)
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
