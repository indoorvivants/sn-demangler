val Version = new {
  val Scalas = Seq("2.13.6")

  val munit = "0.7.29"
}

lazy val root = projectMatrix.in(file(".")).aggregate(demangler, core)

lazy val demangler =
  projectMatrix
    .in(file("modules/cli"))
    .dependsOn(core)
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas, Seq(nativeLinkStubs := true))

lazy val core =
  projectMatrix
    .in(file("modules/core"))
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas, Seq(nativeLinkStubs := true))
    .settings(
      libraryDependencies += "org.scalameta" %%% "munit" % Version.munit
    )
