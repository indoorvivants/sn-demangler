package sn_demangler

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import com.raquo.airstream.web.WebStorageVar
import sn_demangler.Demangler.DemangledSymbolInText

@main def hello =
  val currentYear = new scalajs.js.Date().getFullYear().toInt
  val text = WebStorageVar
    .localStorage("sn-demangler-text", Some(unsafeWindowOwner))
    .text("""
  | _SM7__constG1-0
  | hello world _SM45scala.scalanative.regex.Parser$StringIteratorD4skipiuEO world
  | _SINCORRECT
  """)

  renderOnDomContentLoaded(
    dom.document.getElementById("root"),
    div(
      div(
        cls := "app-container",
        div(
          cls := "panel",
          div(cls := "panel-label", "Input"),
          textArea(
            cls := "text-input",
            value <-- text,
            onInput.mapToValue --> text
          )
        ),
        div(
          cls := "panel",
          div(cls := "panel-label", "Demangled"),
          div(
            cls := "demangled-text",
            pre(
              code(
                children <-- text.signal.map(demangleText(_))
              )
            )
          )
        )
      ),
      footerTag(
        cls := "app-footer",
        p(
          "\u00a9 2021\u2013",
          currentYear.toString,
          " ",
          a(href := "https://indoorvivants.com", "Anton Sviridov")
        )
      )
    )
  )

def demangleText(text: String) =
  val lines = text.split("\n")
  lines.map: line =>
    div:
      val els = List.newBuilder[Element]
      val occs = Demangler
        .demangleAll(line)
      def addText(from: Int, to: Int) =
        els += span(line.slice(from, to))

      def addOcc(d: DemangledSymbolInText) =
        val origLength = d.end - d.start
        if origLength >= d.name.length then
          els += span(cls := "demangled-symbol", d.name)
        else
          val hov = Var(false)
          val shortened = d.name.take(origLength - 1) + "…"
          els += span(
            cls := "demangled-symbol",
            onMouseOver.mapTo(true) --> hov,
            onMouseOut.mapTo(false) --> hov,
            child.text <-- hov.signal.map(if _ then d.name else shortened)
          )

      if occs.length > 0 then
        var curPos = 0
        occs.foreach: occ =>
          addText(curPos, occ.start - 1)
          addOcc(occ)
          curPos = occ.end + 1

        occs.lastOption.foreach: occ =>
          addText(occ.end + 1, line.length)

        els.result()
      else span(line)
