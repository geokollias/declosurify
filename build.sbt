name := "declosurify"

organization := "org.improving"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.10.0-SNAPSHOT"

retrieveManaged := true

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)
