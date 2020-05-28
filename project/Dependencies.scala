import sbt.Keys.libraryDependencies
import sbt._


/**
 * Created by Jacob Xie on 5/20/2020
 */
object Dependencies {

  object Versions {
    val scalaV = "2.12.10"

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
  }

  object Compiles {
    lazy val deps = Seq(
      "org.sangria-graphql" %% "sangria" % Versions.sangriaV,
      "org.sangria-graphql" %% "sangria-slowlog" % Versions.sangiraSlowlogV,
      "org.sangria-graphql" %% "sangria-circe" % Versions.sangiraCirceV,

      "com.typesafe.akka" %% "akka-http" % Versions.akkaHttpV,
      "de.heikoseeberger" %% "akka-http-circe" % Versions.akkaHttpCirceV,

      "io.circe" %% "circe-core" % Versions.circeV,
      "io.circe" %% "circe-parser" % Versions.circeV,
      "io.circe" %% "circe-optics" % Versions.circeOptV,

      "com.typesafe.slick" %% "slick" % Versions.slickV,
      "com.typesafe.slick" %% "slick-hikaricp" % Versions.slickV,
      "org.slf4j" % "slf4j-nop" % Versions.slf4jV,
      "io.underscore" % "slickless_2.12" % Versions.slicklessV,

      "com.h2database" % "h2" % Versions.h2V,
      "org.postgresql" % "postgresql" % Versions.pgV,
      "com.microsoft.sqlserver" % "mssql-jdbc" % Versions.msSqlV

    )
  }

  val deps = libraryDependencies ++= Compiles.deps

  val nexus = "http://192.168.50.130:8091/repository"
}
