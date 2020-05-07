
name := "scala-graphql"

organization := "com.github.jacobbishopxy"
version := "0.1"

description := "scala graphql"

scalaVersion := "2.12.10"
scalacOptions ++= Seq("-deprecation", "-feature")

val sangriaV = "1.4.2"
val sangiraSlowlogV = "0.1.8"
val sangiraCirceV = "1.2.1"

val akkaHttpV = "10.1.3"
val akkaHttpCirceV = "1.21.0"

val circeV = "0.12.3"
val circeOptV = "0.12.0"

val slickV = "3.3.1"
val slf4jV = "1.7.26"
val slicklessV = "0.3.6"

val h2V = "1.4.196"
val pgV = "42.2.9"
val msSqlV = "7.4.1.jre8"

val scalaTestV = "3.0.5"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % sangriaV,
  "org.sangria-graphql" %% "sangria-slowlog" % sangiraSlowlogV,
  "org.sangria-graphql" %% "sangria-circe" % sangiraCirceV,

  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceV,

  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  "io.circe" %% "circe-optics" % circeOptV,

  "com.typesafe.slick" %% "slick" % slickV,
  "com.typesafe.slick" %% "slick-hikaricp" % slickV,
  "org.slf4j" % "slf4j-nop" % slf4jV,
  "io.underscore" % "slickless_2.12" % slicklessV,

  "com.h2database" % "h2" % h2V,
  "org.postgresql" % "postgresql" % pgV,
  "com.microsoft.sqlserver" % "mssql-jdbc" % msSqlV,

  "org.scalatest" %% "scalatest" % scalaTestV % Test
)

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(PackPlugin)

packGenerateWindowsBatFile := false
packJvmOpts := Map("scala-ei-graphql" -> Seq("-Xms8g", "-Xmx8g", "-J-Xss8M"))
