import sbt._
import Keys._
import Process._

object DeclosurifyBuild extends Build {

  val scala = "2.10.0-SNAPSHOT"

  val defaults = Defaults.defaultSettings ++ Seq(
    // scala version + resolver
    scalaVersion := scala,
    scalaBinaryVersion := scala,
    resolvers in ThisBuild += ScalaToolsSnapshots,
    // paths
    scalaSource in Compile <<= baseDirectory(_ / "src"),
    scalaSource in Test <<= baseDirectory(_ / "test"),
    resourceDirectory in Compile <<= baseDirectory(_ / "resources"),
    // sbteclipse needs some info on source directories:
    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
    unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_)),
    // add the library, reflect and the compiler as libraries
    libraryDependencies <<= scalaVersion(ver => Seq(
      "org.scala-lang" % "scala-library" % ver,
      "org.scala-lang" % "scala-reflect" % ver, 
      "org.scala-lang" % "scala-compiler" % ver
    )),
    parallelExecution in Test := false
  )

  // we might need this later
  // val testSettings = Seq(libraryDependencies ++= sMeter, testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"))

  lazy val _declosurify = Project(id = "declosurify",      base = file(".")) aggregate (_decl_base, _decl_test)
  lazy val _decl_base   = Project(id = "declosurify-base", base = file("components/base"), settings = defaults)
  lazy val _decl_test   = Project(id = "declosurify-test", base = file("components/test"), settings = defaults) dependsOn(_decl_base)
}
