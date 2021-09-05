val Version = new {
  val Scala3 = Seq("3.0.2")
  val Scala2 = Seq("2.13.6")
  val Scalas = Scala2 ++ Scala3

  val munit = "0.7.29"
}

lazy val root = projectMatrix
  .in(file("."))
  .aggregate(demangler, core)
  .settings(
    publish / skip := true,
    publishLocal / skip := true
  )

lazy val demangler =
  projectMatrix
    .in(file("modules/cli"))
    .settings(
      moduleName := "sn-demangler"
    )
    .dependsOn(core)
    .jvmPlatform(Version.Scala2)
    .nativePlatform(Version.Scala2, Seq(nativeLinkStubs := true))

lazy val core =
  projectMatrix
    .in(file("modules/core"))
    .settings(moduleName := "sn-demangler-core")
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scala2, Seq(nativeLinkStubs := true))
    .settings(
      libraryDependencies += "org.scalameta" %%% "munit" % Version.munit
    )

inThisBuild(
  Seq(
    organization := "com.indoorvivants",
    organizationName := "Anton Sviridov",
    homepage := Some(url("https://github.com/indoorvivants/sn-demangler")),
    startYear := Some(2021),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "velvetbaldmime@protonmail.com",
        url("https://blog.indoorvivants.com")
      )
    )
  )
)
