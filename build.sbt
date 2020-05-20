
ThisBuild / name := "scala-graphql"

lazy val commonSettings = Seq(
  organization := "com.github.jacobbishopxy",
  scalaVersion := Dependencies.Versions.scalaV,
  Dependencies.deps
)

lazy val rootConfig = Seq(
  version := "0.1"
)

lazy val root = Project(id = "scala-graphql", base = file("."))
  .enablePlugins(PackPlugin)
  .settings(commonSettings, rootConfig)

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(PackPlugin)

packGenerateWindowsBatFile := false
packJvmOpts := Map("scala-ei-graphql" -> Seq("-Xms8g", "-Xmx8g", "-J-Xss8M"))
