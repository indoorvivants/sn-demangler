import scala.scalanative.build.LTO
import scala.scalanative.build.Mode

Global / onChangedBuildSource := ReloadOnSourceChanges
val Version = new {
  val Scala3 = "3.3.7"
  val Scala3_Next = "3.8.3"
  val Scala2 = "2.13.17"
  val Scalas = Seq(Scala3, Scala2)
  val parsley = "5.0.0-M18"
  val laminar = "17.2.1"
}

lazy val root = projectMatrix
  .in(file("."))
  .aggregate(lib, bin)
  .settings(
    publish / skip := true,
    Compile / doc / skip := true,
    publishLocal / skip := true
  )

lazy val webapp = projectMatrix
  .in(file("mod/webapp"))
  .enablePlugins(ScalaJSPlugin, ForgeViteWebappPlugin)
  .dependsOn(lib)
  .jsPlatform(Seq(Version.Scala3))
  .settings(
    libraryDependencies += "com.raquo" %%% "laminar" % Version.laminar
  )

lazy val bin =
  projectMatrix
    .in(file("mod/bin"))
    .settings(
      moduleName := "sn-demangler",
      nativeLinkReleaseFast / nativeConfig := {
        import com.indoorvivants.detective.*
        nativeConfig.value
          .withOptimize(true)
          .withLTO(
            if (Platform.os == Platform.OS.MacOS) LTO.none else LTO.thin
          )
      }
    )
    .dependsOn(lib)
    .jvmPlatform(Version.Scalas)
    .nativePlatform(
      Seq(Version.Scala3),
      Seq.empty,
      _.enablePlugins(ForgeNativeBinaryPlugin).settings(
        buildBinaryConfig ~= { (_).withName("sn-demangler") }
      )
    )

lazy val lib =
  projectMatrix
    .in(file("mod/lib"))
    .settings(moduleName := "sn-demangler-core")
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas)
    .jsPlatform(Version.Scalas)
    .settings(
      libraryDependencies += "org.scalameta" %%% "munit" % "1.2.1" % Test
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
