name := """sdkman-hooks-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "info.cukes" %% "cucumber-scala" % "1.2.5" % Test,
  "info.cukes" % "cucumber-junit" % "1.2.5" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test
)

