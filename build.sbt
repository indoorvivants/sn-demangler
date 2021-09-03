val Version = new {
  val Scalas = Seq("2.13.6")
}

lazy val root = project.in(file(".")).aggregate(demangler.projectRefs: _*)

lazy val demangler =
  projectMatrix
    .in(file("modules/core"))
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas, Seq(nativeLinkStubs := true))
