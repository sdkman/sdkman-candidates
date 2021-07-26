import com.typesafe.config.ConfigFactory
import sbt.Resolver

enablePlugins(JavaServerAppPackaging)

enablePlugins(DockerPlugin)

name := "sdkman-candidates"

Docker / packageName := "sdkman/sdkman-hooks"

dockerBaseImage := "openjdk:11"

Universal / javaOptions ++= Seq(
  "-Dpidfile.path=/dev/null"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.13"

routesGenerator := InjectedRoutesGenerator

resolvers ++= Seq(
  Resolver.mavenCentral,
  "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= Seq(
  guice,
  ws,
  "com.github.sdkman" % "sdkman-mongodb-persistence" % "1.9",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  "info.cukes" %% "cucumber-scala" % "1.2.5" % Test,
  "info.cukes" % "cucumber-junit" % "1.2.5" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test,
  "org.pegdown" % "pegdown" % "1.6.0" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % Test
)

logBuffered in Test := false

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/reports/scalatest/")

scalacOptions += "-Ypartial-unification"
