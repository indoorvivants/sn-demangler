import scala.scalanative.build.LTO
import scala.scalanative.build.Mode
import demangler.build.Platform

Global / onChangedBuildSource := ReloadOnSourceChanges
val Version = new {
  val Scala3 = "3.2.1"
  val Scala2 = "2.13.15"
  val Scalas = Seq(Scala3, Scala2)
}

lazy val root = projectMatrix
  .in(file("."))
  .aggregate(cli, core)
  .settings(
    publish / skip := true,
    Compile / doc / skip := true,
    publishLocal / skip := true
  )

lazy val cli =
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
            .withLTO(
              if (Platform.os == Platform.OS.MacOS) LTO.none else LTO.thin
            )
        else nativeConfig.value
      }
    )
    .dependsOn(core)
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas)

lazy val core =
  projectMatrix
    .in(file("modules/core"))
    .settings(moduleName := "sn-demangler-core")
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas)
    .settings(
      Compile / doc / scalacOptions ~= { opts =>
        opts.filterNot(_.contains("-Xplugin"))
      },
      libraryDependencies += "com.eed3si9n.verify" %%% "verify" % "1.0.0" % Test,
      testFrameworks += new TestFramework("verify.runner.Framework")
    )

lazy val buildNativeBinary = taskKey[File]("")

buildNativeBinary := {
  val built = (cli.native(Version.Scala3) / Compile / nativeLink).value
  val name =
    if (Platform.os == Platform.OS.Windows) "demangler.exe" else "demangler"
  val dest = (ThisBuild / baseDirectory).value / "bin" / name

  IO.copyFile(built, dest)

  dest
}

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
