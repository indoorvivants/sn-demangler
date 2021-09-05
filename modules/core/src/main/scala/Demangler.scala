package com.indoorvivants.demangler

import java.io.File
import scala.util.Try
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import scala.collection.mutable.ArrayBuffer

object Demangler {
  private class CursorWithResult(
      original: String,
      acc: StringBuilder
  ) {
    private val len = original.length

    private var position = 0

    private var redirect: StringBuilder = null

    def setupRedirect(sb: StringBuilder) = redirect = sb

    def removeRedirect() = redirect = null

    def move =
      if (position < len - 1) { position += 1; this }
      else err("Could not move to next character")
    
    def peekSafe: Option[Char] =
      if (position >= len - 1) None else Some(peek)
    
    def peek = original(position + 1)
    
    def current = original(position)
    
    def append(s: String) = {
      (if (redirect == null) acc else redirect).append(s)
      this
    }

    def append(c: Char) = {
      (if (redirect == null) acc else redirect).append(c)
      this
    }

    def result: String = {
      if (position != len - 1)
        err("Not the entire string was consumed!")
      else
        acc.result()
    }

    def repr = {
      val st = new StringBuilder

      st.append(original + "\n")
      st.append(" " * (position) + "^\n")
      st.append("Accumulated: " + acc.result() + "\n")

      st.result()
    }

    class Error(s: String) extends Exception(s)

    def err(s: String) = throw new Error(s + "\n\n" + repr)
    def err(ex: Throwable) = throw new Exception(ex)

  }

  def demangle(symbol: String) = {
    val cr = new CursorWithResult(symbol, new StringBuilder)

    try {
      Impl.mangled_name(cr)

      cr.result
    } catch {
      case NonFatal(e) =>
        cr.err(e)
    }
  }

  private object Impl {

    def err(s: String)(implicit cr: CursorWithResult) = throw new Exception(s)

    def mangled_name(cursor: CursorWithResult) = {
      if (cursor.current == '_' && cursor.peekSafe.contains('S')) {
        defn_name(cursor.move.move)
      } else
        cursor.err("Expected identifier to start with _S")
    }

    def integer_type_name(cursor: CursorWithResult) = {
      cursor.current match {
        case 'b'   => cursor.append("Byte")
        case 's'   => cursor.append("Short")
        case 'i'   => cursor.append("Int")
        case 'j'   => cursor.append("Long")
        case other => cursor.err(s"Unknown integer type '$other'")
      }

      cursor.move
    }

    def scope(cursor: CursorWithResult) = cursor.current match {
      case 'O' => ()
      case 'P' =>
        cursor.append("private[")
        defn_name(cursor.move)
        cursor.append("]")
    }

    def defn_name(cursor: CursorWithResult): Unit =
      if (cursor.current == 'M') member_name(cursor.move)
      else if (cursor.current == 'T') top_level_name(cursor.move)
      else cursor.err(s"Expected either M or T, got ${cursor.current} instead")

    def top_level_name(cursor: CursorWithResult) = {
      name(cursor)
    }

    def member_name(cursor: CursorWithResult) = {
      name(cursor)
      cursor.append(".")
      sig_name(cursor)
    }

    def sig_name(cursor: CursorWithResult) = cursor.current match {
      case 'R' =>
        cursor.move
        cursor.append("<constructor>(")
        while (cursor.current != 'E') {
          type_name(cursor)
          if (cursor.current == 'E') cursor.append(')')
          else cursor.append(',')
        }

      case 'C' =>
        cursor.append("<extern> ")
        name(cursor.move)
      case 'G' =>
        cursor.append("<generated> ")
        name(cursor.move)
      case 'F' =>
        name(cursor.move)
        scope(cursor)
      case 'D' =>
        cursor.move
        name(cursor)
        val buf = new ArrayBuffer[StringBuilder]
        while (cursor.current != 'E') {
          val newSB = new StringBuilder
          buf.append(newSB)
          cursor.setupRedirect(newSB)
          type_name(cursor)
          cursor.removeRedirect()
        }

        if(buf.size == 1) {
          cursor.append("()")
          cursor.append(": ")
          cursor.append(buf.head.result())
        } else {
          val (arguments, return_type) = buf.splitAt(buf.size-1)
          
          cursor.append('(')
          var i = 0 
          while(i < arguments.size) {
            cursor.append(arguments(i).result())
            if(i != arguments.size - 1) cursor.append(", ")
            i+=1
          }
          cursor.append(')')
          cursor.append(": " + return_type.head.result())
        }

        cursor.move

        scope(cursor)
    }

    def type_name(cursor: CursorWithResult): Unit = cursor.current match {
      case 'v' =>
        cursor.append("<C vararg>")
        cursor.move
      case 'R' =>
        if (cursor.peek == '_') {
          cursor.append("<c-pointer*>")
          cursor.move.move
        }
      case 'z' => cursor.append("Boolean").move
      case 'c' => cursor.append("Char").move
      case 'f' => cursor.append("Float").move
      case 'd' => cursor.append("Double").move
      case 'u' => cursor.append("Unit").move
      case 'l' => cursor.append("Null").move
      case 'n' => cursor.append("Nothing").move
      case 'L' =>
        nullable_type_name(cursor.move)
      case 'A' =>
        cursor.append("Array[")
        type_name(cursor.move)
        cursor.append("]")
        if (cursor.current != '_')
          cursor.err(
            s"Expected _ after Array definition, got ${cursor.current} instead"
          )
        cursor.move

      case 'X' =>
        name(cursor.move)
      case n if n.isDigit =>
        name(cursor.move)
      case i =>
        integer_type_name(cursor)
    }

    def nullable_type_name(cursor: CursorWithResult): Unit =
      cursor.current match {
        case 'A' =>
          type_name(cursor.move)
          if (cursor.current != '_')
            cursor.err(
              s"Expected _ after Array definition, got ${cursor.current} instead"
            )
          cursor.move
        case 'X' => name(cursor.move)
        case _   => name(cursor)
      }

    def name(cursor: CursorWithResult) = {
      val length = new StringBuilder

      while (cursor.current.isDigit) {
        length.append(cursor.current);
        cursor.move
      }

      val len = length.result().toInt

      var i = 0

      val next = if (cursor.current == '-') { cursor.append('-'); cursor.move }
      else cursor
      while (i < len) {
        cursor.append(cursor.current);
        cursor.peekSafe.foreach { _ => cursor.move }
        i += 1
      }
    }

  }

}
