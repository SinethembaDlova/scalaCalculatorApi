name := "calculatorApi"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val specs2V  = "4.0.2"
  val akkaV       = "2.5.3"
  val circeV      = "0.9.0"
  val monocleVersion = "1.5.0-cats-M1"
  val akkaHttpV = "10.1.0-RC1"
  val monixV = "3.0.0-M3"
  val catsV = "1.0.1"
  val jwtV = "0.4.5"
  Seq(
    "com.jason-goodwin" %% "authentikat-jwt" % jwtV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "org.typelevel" %% "cats-core" % catsV,
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
    "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
    "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test",
    "org.specs2" %% "specs2-core" % specs2V % "test",
    "io.monix" %% "monix" % monixV,
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}