package todo

import cats.effect._
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import todo.config.AppConfig
import todo.http.TodoAPI
import todo.repository.{DoobieTodoRepository, TodoRepository}
import todo.tracing.JaegerTracer
import zio.interop.catz._
import zio.telemetry.opentelemetry.Tracing
import zio.{ExitCode => ZExitCode, _}

object Main extends zio.ZIOAppDefault {
  type ENV =  AppConfig with TodoRepository with zio.Clock with Tracing.Service
  type AppTask[A] = RIO[ENV, A]

  override def run: ZIO[ZEnv, Nothing, ZExitCode] = {

    val prog: ZIO[ENV, Throwable, ZExitCode] =
      for {
        cfg <- ZIO.service[AppConfig]
        // _ <- logging.log.info(s"Starting with $cfg")
        httpApp = Router[AppTask](
          "/todos" -> new TodoAPI().routes(s"${cfg.http.baseUrl}/todos")
        ).orNotFound

        _ <- runHttp(httpApp, cfg.http.port)
      } yield ZExitCode.success

    val tracing = AppConfig.live >>> JaegerTracer.live >>> Tracing.live
    val app =
      tracing ++ zio.Clock.live ++ AppConfig.live ++ (AppConfig.live ++ zio.Clock.live ++ tracing >>> DoobieTodoRepository.layer)
    prog
      .provideLayer(
        app
      )
      .orDie
  }

  def runHttp[R <: zio.Clock](
      httpApp: HttpApp[RIO[R, *]],
      port: Int
  ): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder[Task]
        .withExecutionContext(rts.runtimeConfig.executor.asExecutionContext)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(CORS.policy.withAllowOriginAll(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }
}
