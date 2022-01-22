package com.indoorvivants.demangler

import java.io.File
import scala.util.Try
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import scala.util.matching.Regex

object DemanglerApp {

  def main(args: Array[String]): Unit = {

    args.headOption match {
      case None =>
        sys.error(
          "Please specify mode: -f <file>, -s <identifies>, -i for stdin input"
        )

      case Some("-f") =>
        args.tail.headOption match {
          case None => sys.error("Please specify filename")
          case Some(f) =>
            print(iterator(scala.io.Source.fromFile(new File(f)).getLines()))
        }

      case Some("-s") =>
        print(iterator(args.tail.toIterator))

      case Some("-i") =>
        print(iterator(Iterator.continually(scala.io.StdIn.readLine())))

      case Some("-ll") =>
        args.tail.headOption match {
          case None => sys.error("Please specify filename")
          case Some(f) =>
            val it = scala.io.Source.fromFile(new File(f)).getLines()

            print {
              it.map { line =>
                """ "(_S.*?)"  """.trim.r.replaceAllIn(
                  line,
                  { m =>
                    try {
                      '"' + Regex.quoteReplacement(
                        Demangler.demangle(m.group(1))
                      ) + '"'
                    } catch {
                      case NonFatal(_) => Regex.quoteReplacement(m.group(0))
                    }
                  }
                )
              }
            }
        }
      case Some(other) =>
        sys.error(s"Unreckognized mode $other, choose one of -f, -i, or -s")
    }

  }

  private def iterator(it: Iterator[String]): Iterator[String] =
    it.map(Demangler.demangle)

  private def print(it: Iterator[String]): Unit =
    it.foreach(println)

}
