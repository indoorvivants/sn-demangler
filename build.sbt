import scala.scalanative.build.LTO
import scala.scalanative.build.Mode

Global / onChangedBuildSource := ReloadOnSourceChanges
val Version = new {
  val Scala3 = "3.3.5"
  val Scala2 = "2.13.16"
  val Scalas = Seq(Scala3, Scala2)
}

lazy val root = projectMatrix
  .in(file("."))
  .aggregate(lib, bin)
  .settings(
    publish / skip := true,
    Compile / doc / skip := true,
    publishLocal / skip := true
  )

lazy val bin =
  projectMatrix
    .in(file("mod/bin"))
    .settings(
      moduleName := "sn-demangler",
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
    .dependsOn(lib)
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas)

lazy val lib =
  projectMatrix
    .in(file("mod/lib"))
    .settings(moduleName := "sn-demangler-core")
    .jvmPlatform(Version.Scalas)
    .nativePlatform(Version.Scalas)
    .jsPlatform(Version.Scalas)
    .settings(
      libraryDependencies += "com.eed3si9n.verify" %%% "verify" % "1.0.0" % Test,
      testFrameworks += new TestFramework("verify.runner.Framework")
    )

import com.indoorvivants.detective.Platform
lazy val buildBinary = taskKey[File]("")
buildBinary := {
  writeBinary(
    source = (bin.native(Version.Scala3)  / Compile / nativeLink).value,
    destinationDir = (ThisBuild / baseDirectory).value / "out" / "debug",
    log = sLog.value,
    platform = None,
    debug = true
  )
}

lazy val buildReleaseBinary = taskKey[File]("")
buildReleaseBinary := {
  writeBinary(
    source = (bin.native(Version.Scala3) / Compile / nativeLinkReleaseFast).value,
    destinationDir = (ThisBuild / baseDirectory).value / "out" / "release",
    log = sLog.value,
    platform = None,
    debug = false
  )
}

lazy val buildPlatformBinary = taskKey[File]("")
buildPlatformBinary := {
  writeBinary(
    source = (bin.native(Version.Scala3) / Compile / nativeLinkReleaseFast).value,
    destinationDir = (ThisBuild / baseDirectory).value / "out" / "release",
    log = sLog.value,
    platform = Some(Platform.target),
    debug = false
  )
}

val BINARY_NAME = "sn-demangler"

def writeBinary(
    source: File,
    destinationDir: File,
    log: sbt.Logger,
    platform: Option[Platform.Target],
    debug: Boolean
): File = {

  val name = platform match {
    case None => "app"
    case Some(target) =>
      val ext = target.os match {
        case Platform.OS.Windows => ".exe"
        case _                   => ""
      }

      BINARY_NAME + "-" + ArtifactNames.coursierString(target) + ext
  }

  val dest = destinationDir / name

  IO.copyFile(source, dest, CopyOptions.apply(true, true, true))

  import scala.sys.process.*

  if (debug && platform.exists(_.os == Platform.OS.MacOS))
    s"dsymutil $dest".!!

  log.info(s"Binary [$name] built in ${dest}")

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
