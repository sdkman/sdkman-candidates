import sbt.Resolver

enablePlugins(JavaServerAppPackaging)

enablePlugins(DockerPlugin)

name := "sdkman-candidates"

Docker / packageName := "sdkman/sdkman-candidates"

dockerBaseImage := "openjdk:11"

dockerExposedPorts ++= Seq(9000)

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
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % Test,
  "io.cucumber" %% "cucumber-scala" % "4.7.1" % Test,
  "io.cucumber" % "cucumber-junit" % "4.7.1" % Test,
  "info.cukes" % "gherkin" % "2.7.3" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test,
  "org.pegdown" % "pegdown" % "1.6.0" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % Test
)

(Test / logBuffered) := false

(Test / testOptions) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/reports/scalatest/")

scalacOptions += "-Ypartial-unification"

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(releaseStepTask((Docker / publish))),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
