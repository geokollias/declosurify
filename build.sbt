name := "declosurify"

organization := "org.improving"

version := "0.1.1"

scalaVersion := "2.10.0-RC1"

scalaBinaryVersion := "2.10.0-RC1"

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)
