addCommandAlias("build", "prepare; test")
addCommandAlias("prepare", "fix; fmt")
addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "compile:scalafix --check; test:scalafix --check"
)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "fmtCheck",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalaVersion := "2.13.16",
    scalafixDependencies ++= Dependencies.ScalaFix
  )
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    Docker / dockerExposedPorts := Seq(8080),
    libraryDependencies ++= Dependencies.App,
    name := "zio-expedia",
    ThisBuild / scalacOptions := Options
      .scalacOptions(scalaVersion.value, isSnapshot.value),
    testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
