import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AsciiBirdProject extends Build {

  val ScalaVersion = "2.10.3"

  val root = Project(
    "root",
    file("."),
    settings = Defaults.defaultSettings ++ assemblySettings ++ Seq(
      scalaVersion := ScalaVersion,
      fork in run := true,
      mainClass in (Compile, run) := None,
      mainClass in assembly := Some("AsciiBird"),
      jarName in assembly := "asciibird.jar",
      libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
        "org.mockito" % "mockito-core" % "1.9.5" % "test",
        "org.slf4j" % "slf4j-api" % "1.6.1",
        "org.slf4j" % "log4j-over-slf4j" % "1.6.1",
        "ch.qos.logback" % "logback-classic" % "0.9.24",
        "com.googlecode.lanterna" % "lanterna" % "2.1.7",
        "jline" % "jline" % "2.11"
      )
    )
  )
}
