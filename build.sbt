import com.typesafe.config.ConfigFactory
import sbt.Resolver

enablePlugins(JavaServerAppPackaging)

enablePlugins(DockerPlugin)

name := "sdkman-candidates"

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("application.version")

packageName in Docker := "sdkman/sdkman-candidates"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.9"

routesGenerator := InjectedRoutesGenerator

resolvers ++= Seq(
  Resolver.bintrayRepo("sdkman", "maven"),
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "io.sdkman" %% "sdkman-mongodb-persistence" % "0.11",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  "info.cukes" %% "cucumber-scala" % "1.2.5" % Test,
  "info.cukes" % "cucumber-junit" % "1.2.5" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.6" % Test,
  "org.pegdown" % "pegdown" % "1.6.0" % Test
)

logBuffered in Test := false

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/reports/scalatest/")

scalacOptions += "-Ypartial-unification"