import scala.scalanative.build.LTO
import scala.scalanative.build.Mode
Global / onChangedBuildSource := ReloadOnSourceChanges

val Version = new {
  val Scala3 = Seq("3.1.1")
  val Scala2 = Seq("2.13.8")
  val Scalas = Scala2 ++ Scala3

  val munit = "0.7.29"
}

lazy val root = projectMatrix
  .in(file("."))
  .aggregate(demangler, core)
  .settings(
    publish / skip := true,
    Compile / doc / skip := true,
    publishLocal / skip := true
  )

lazy val demangler =
  projectMatrix
    .in(file("modules/cli"))
    .settings(
      moduleName := "sn-demangler",
      Compile / doc / scalacOptions ~= { opts =>
        opts.filterNot(_.contains("-Xplugin"))
      },
      nativeConfig := {
        if (sys.env.get("SN_RELEASE").contains("fast"))
          nativeConfig.value
            .withMode(Mode.releaseFast)
            .withOptimize(true)
            .withLTO(LTO.thin)
        else nativeConfig.value
      }
    )
    .dependsOn(core)
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas, Seq(nativeLinkStubs := true))

lazy val core =
  projectMatrix
    .in(file("modules/core"))
    .settings(moduleName := "sn-demangler-core")
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas, Seq(nativeLinkStubs := true))
    .settings(
      Compile / doc / scalacOptions ~= { opts =>
        opts.filterNot(_.contains("-Xplugin"))
      },
      libraryDependencies += "com.eed3si9n.verify" %%% "verify" % "1.0.0" % Test,
      testFrameworks += new TestFramework("verify.runner.Framework")
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
