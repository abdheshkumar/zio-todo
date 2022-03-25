package todo.http

import cats.data.{Kleisli, OptionT}
import io.circe.{Decoder, Encoder}
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import io.opentelemetry.context.propagation.{
  TextMapGetter,
  TextMapPropagator,
  TextMapSetter
}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.ci.CIString
import todo.repository.TodoRepository
import todo.{TodoId, TodoItemPatchForm, TodoItemPostForm}
import zio._
import zio.interop.catz._
import zio.telemetry.opentelemetry.Tracing
import zio.telemetry.opentelemetry.Tracing.root

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class TodoService[R <: TodoRepository with Tracing.Service] {
  type TodoTask[A] = RIO[R, A]

  val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
  import dsl._

  val propagator: TextMapPropagator = W3CTraceContextPropagator.getInstance()
  val getter: TextMapGetter[Headers] = new TextMapGetter[Headers] {
    override def keys(carrier: Headers): java.lang.Iterable[String] =
      carrier.headers.map(_.name.toString).asJava

    override def get(carrier: Headers, key: String): String =
      carrier.headers.find(_.name.toString == key).map(_.name.toString).orNull
  }
  val errorMapper: PartialFunction[Throwable, StatusCode] = { case _ =>
    StatusCode.UNSET
  }
  val setter: TextMapSetter[mutable.Map[String, String]] =
    (carrier, key, value) => carrier.update(key, value)

  def tracingService(
      service: HttpRoutes[TodoTask]
  ): HttpRoutes[TodoTask] = Kleisli { (req: Request[TodoTask]) =>
    val response =
      root(s"HTTP ${req.method.name} ${req.uri.renderString}", SpanKind.SERVER, errorMapper) {
        for {
          _ <- Tracing.addEvent("event from backend before response")
          carrier <- UIO(mutable.Map[String, String]().empty)
          _ <- Tracing.setAttribute("http.method", s"${req.method.name}")
          //_ <- Tracing.inject(propagator, carrier, setter)
          response <- service(
            req.withHeaders(
              carrier.toList.map(v => Header.Raw(CIString(v._1), v._2))
            )
          ).value
          res <- response match {
            case Some(value) => RIO.succeed(value)
            case None        => RIO.succeed(Response[TodoTask](Status.NotFound))
          }
          _ <- Tracing.addEvent("event from backend after response")

        } yield res
      }

    OptionT.liftF(response)

  }

  def routes(rootUri: String): HttpRoutes[RIO[R, *]] = tracingService {

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[TodoTask, A] =
      jsonOf[TodoTask, A]

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[TodoTask, A] =
      jsonEncoderOf[TodoTask, A]

    HttpRoutes.of[TodoTask] {
      case GET -> Root / LongVar(id) =>
        for {
          todo <- TodoRepository(_.getById(TodoId(id)))
          response <- todo.fold(NotFound())(x =>
            Ok(TodoItemWithUri(rootUri, x))
          )
        } yield response

      case GET -> Root =>
        Ok(TodoRepository(_.getAll).map(_.map(TodoItemWithUri(rootUri, _))))

      case req @ POST -> Root =>
        req.decode[TodoItemPostForm] { todoItemForm =>
          TodoRepository(_.create(todoItemForm))
            .map(TodoItemWithUri(rootUri, _))
            .flatMap(Created(_))
        }

      case DELETE -> Root / LongVar(id) =>
        for {
          item <- TodoRepository(_.getById(TodoId(id)))
          result <- item
            .map(x => TodoRepository(_.delete(x.id)))
            .fold(NotFound())(_.flatMap(Ok(_)))
        } yield result

      case DELETE -> Root =>
        TodoRepository(_.deleteAll) *> Ok()

      case req @ PATCH -> Root / LongVar(id) =>
        req.decode[TodoItemPatchForm] { updateForm =>
          for {
            update <- TodoRepository(_.update(TodoId(id), updateForm))
            response <- update.fold(NotFound())(x =>
              Ok(TodoItemWithUri(rootUri, x))
            )
          } yield response
        }
    }
  }
}
