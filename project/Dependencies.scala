import sbt._

object Dependencies {

  object Versions {
    val betterMonadicFor = "0.3.1"
    val circe = "0.14.10"
    val doobie = "1.0.0-RC6"
    val flyway = "10.22.0"
    val h2 = "2.3.232"
    val http4s = "0.23.28"
    val http4sBlaze ="0.23.16"
    val jawn = "1.6.0"
    val kindProjector = "0.13.3"
    val log4j = "2.24.3"
    val organizeImports = "0.6.0"
    val pureConfig = "0.17.7"
    val zio = "2.1.11"
    val zioOpentelemetry = "2.0.3"
    val zioInteropCats = "23.1.0.3"
    val zioLogging = "2.3.2"
    val opentelemetry = "1.27.0"

  }
  import Versions._

  val App =
    List(
      "com.github.pureconfig" %% "pureconfig" % pureConfig,
      "com.h2database" % "h2" % h2,
      "dev.zio" %% "zio-interop-cats" % zioInteropCats,
      "dev.zio" %% "zio-logging-slf4j" % zioLogging,
      "dev.zio" %% "zio-logging" % zioLogging,
      "dev.zio" %% "zio-test-sbt" % zio % "test",
      "dev.zio" %% "zio-test" % zio % "test",
      "dev.zio" %% "zio-opentelemetry" % zioOpentelemetry,
      "io.opentelemetry" % "opentelemetry-exporter-jaeger" % opentelemetry,
      "dev.zio" %% "zio" % zio,
      "io.circe" %% "circe-core" % circe,
      "io.circe" %% "circe-generic" % circe,
      "io.circe" %% "circe-literal" % circe % "test",
      "org.apache.logging.log4j" % "log4j-api" % log4j,
      "org.apache.logging.log4j" % "log4j-core" % log4j,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j,
      "org.flywaydb" % "flyway-core" % flyway,
      "org.http4s" %% "http4s-blaze-server" % http4sBlaze,
      "org.http4s" %% "http4s-circe" % http4s,
      "org.http4s" %% "http4s-dsl" % http4s,
      "org.tpolecat" %% "doobie-core" % doobie,
      "org.tpolecat" %% "doobie-h2" % doobie,
      "org.tpolecat" %% "doobie-hikari" % doobie,
      "org.typelevel" %% "jawn-parser" % jawn % "test",
      compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicFor),
      compilerPlugin(
        ("org.typelevel" % "kind-projector" % kindProjector).cross(
          CrossVersion.full
        )
      ),
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-16" % "3.2.14.0" % Test
    )

  val ScalaFix =
    List(
      "com.github.liancheng" %% "organize-imports" % organizeImports
    )

}
