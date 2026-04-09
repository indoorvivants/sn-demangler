package sn_demangler

import java.io.File
import scala.util.Try
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import scala.util.matching.Regex
import java.nio.file.Files
import java.nio.file.Paths

object DemanglerApp {

  import decline_derive.*
  @Help("Scala Native demangler")
  case class Config(
    @Help("Treat text as name of file (use `-` to read from STDIN)")
    @Short("f")
    file: Boolean,
    @Help("Don't use ASCII colors")
    plain: Boolean,
    @Positional("text")
    text: String
  ) derives CommandApplication

  def main(args: Array[String]): Unit = {
    val config = CommandApplication.parseOrExit[Config](args)

    val input =
      if config.file then
        config.text match
          case "-" => scala.io.Source.stdin.getLines().mkString("\n")
          case filename => new String(Files.readAllBytes(Paths.get(filename)))
      else
        config.text

    val plain = config.plain || sys.env.get("TERM").contains("dumb") || sys.env.contains("NO_COLOR")

    demangleText(input).foreach {segs =>
      println(segs.map {s =>
        if s.highlight then
          if plain then s.value else Console.YELLOW + s.value + Console.RESET
        else
          s.value
      }.mkString)
    }
  }
}

case class Segment(value: String, highlight: Boolean)

def demangleText(text: String) = {
  val lines = text.split("\n").toList
  lines.map{ line =>
      val els = List.newBuilder[Segment]
      val occs = Demangler.demangleAll(line)

      def addText(from: Int, to: Int) =
        els += Segment(line.slice(from, to), false)

      def addOcc(d: Demangler.DemangledSymbolInText) =
        els += Segment(d.name, true)

      if (occs.length > 0) {
        var curPos = 0
        occs.foreach: occ =>
          addText(curPos, occ.start)
          addOcc(occ)
          curPos = occ.end + 1

        occs.lastOption.foreach: occ =>
          addText(occ.end + 1, line.length)

        els.result()
      } else List(Segment(line, false))
  }
}
