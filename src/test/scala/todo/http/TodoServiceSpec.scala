package todo.http

import io.circe.Decoder
import io.circe.literal._
import io.opentelemetry.api.OpenTelemetry
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.{Status, _}
import todo.http.HTTPSpec._
import todo.repository.{InMemoryTodoRepository, TodoRepository}
import zio._
import zio.interop.catz._
import zio.telemetry.opentelemetry.Tracing
import zio.test._

object TodoServiceSpec extends DefaultRunnableSpec {
  type TodoTask[A] = RIO[TodoRepository with Tracing.Service, A]

  val app =
    new TodoAPI[TodoRepository with Tracing.Service]().routes("").orNotFound

  override def spec =
    suite("TodoService")(
      test("should create new todo items") {
        val req = request[TodoTask](Method.POST, "/")
          .withEntity(json"""{"title": "Test"}""")
        checkRequest(
          app.run(req),
          Status.Created,
          Some(json"""{
            "id": 1,
            "url": "/1",
            "title": "Test",
            "completed":false,
            "order":null
          }""")
        )
      },
      test("should list all todo items") {
        val setupReq =
          request[TodoTask](Method.POST, "/")
            .withEntity(json"""{"title": "Test"}""")
        val req = request[TodoTask](Method.GET, "/")
        checkRequest(
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(json"""[
              {"id": 1, "url": "/1", "title": "Test", "completed":false, "order":null},
              {"id": 2, "url": "/2", "title": "Test", "completed":false, "order":null}
            ]""")
        )
      },
      test("should delete todo items by id") {
        val setupReq =
          request[TodoTask](Method.POST, "/")
            .withEntity(json"""{"title": "Test"}""")
        val deleteReq =
          (id: Long) => request[TodoTask](Method.DELETE, s"/$id")
        val req = request[TodoTask](Method.GET, "/")
        checkRequest(
          app
            .run(setupReq)
            .flatMap { resp =>
              implicit def circeJsonDecoder[A](implicit
                  decoder: Decoder[A]
              ): EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
              resp.as[TodoItemWithUri].map(_.id)
            }
            .flatMap(id => app.run(deleteReq(id))) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      },
      test("should delete all todo items") {
        val setupReq =
          request[TodoTask](Method.POST, "/")
            .withEntity(json"""{"title": "Test"}""")
        val deleteReq = request[TodoTask](Method.DELETE, "/")
        val req = request[TodoTask](Method.GET, "/")
        checkRequest(
          app.run(setupReq) *> app.run(setupReq) *> app
            .run(deleteReq) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      },
      test("should update todo items") {
        val setupReq =
          request[TodoTask](Method.POST, "/")
            .withEntity(json"""{"title": "Test"}""")
        val updateReq =
          (id: Long) =>
            request[TodoTask](Method.PATCH, s"/$id")
              .withEntity(json"""{"title": "Test1"}""")
        val req = request[TodoTask](Method.GET, "/")
        checkRequest(
          app
            .run(setupReq)
            .flatMap { resp =>
              implicit def circeJsonDecoder[A: Decoder]
                  : EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
              resp.as[TodoItemWithUri].map(_.id)
            }
            .flatMap(id => app.run(updateReq(id))) *> app.run(req),
          Status.Ok,
          Some(json"""[
              {"id": 1, "url": "/1", "title": "Test1", "completed":false, "order":null}
            ]""")
        )
      }
    ).provideSomeLayer[ZEnv](
      InMemoryTodoRepository.layer ++ (ZLayer.succeed(
        OpenTelemetry.noop().getTracer("test")
      ) >>> Tracing.live)
    )
}
