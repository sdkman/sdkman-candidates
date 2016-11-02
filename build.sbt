import com.typesafe.config.ConfigFactory

enablePlugins(JavaServerAppPackaging)

enablePlugins(DockerPlugin)

name := """sdkman-candidates"""

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("application.version")

packageName in Docker := "sdkman/sdkman-candidates"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  "info.cukes" %% "cucumber-scala" % "1.2.5" % Test,
  "info.cukes" % "cucumber-junit" % "1.2.5" % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test
)

