package com.indoorvivants.demangler

import java.io.File
import scala.util.Try
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal

object Main {

  def main(args: Array[String]): Unit = {

    val iterator = args.headOption match {
      case None =>
        sys.error(
          "Please specify mode: -f <file>, -s <identifies>, -i for stdin input"
        )

      case Some("-f") =>
        args.tail.headOption match {
          case None    => sys.error("Please specify filename")
          case Some(f) => scala.io.Source.fromFile(new File(f)).getLines()
        }

      case Some("-s") =>
        args.tail.toIterator

      case Some("-i") => Iterator.continually(scala.io.StdIn.readLine())
      case Some(other) => sys.error(s"Unreckognized mode $other, choose one of -f, -i, or -s")
    }

    iterator.foreach { line =>
      println(Demangler.demangle(line))
    }

  }
}

