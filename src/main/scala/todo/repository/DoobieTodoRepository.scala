package todo.repository

import cats.effect.std.Dispatcher
import cats.implicits._
import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import org.flywaydb.core.Flyway
import todo._
import todo.config.{AppConfig, DatabaseConfig}
import zio._
import zio.interop.catz._
import zio.telemetry.opentelemetry.Tracing

import scala.concurrent.Future

final private class DoobieTodoRepository(
    xa: Transactor[Task],
    tracing: Tracing.Service
) extends TodoRepository {
  import DoobieTodoRepository.SQL

  val errorMapper: PartialFunction[Throwable, StatusCode] = { case _ =>
    StatusCode.ERROR
  }

  override def getAll: Task[List[TodoItem]] = {
    Tracing
      .span(
        "getAll",
        SpanKind.INTERNAL,
        errorMapper
      )(
        SQL.getAll
          .to[List]
          .transact(xa)
      )
      .provideService(tracing)
  }

  override def getById(id: TodoId): Task[Option[TodoItem]] = Tracing
    .span(
      "getById",
      SpanKind.INTERNAL,
      errorMapper
    )(
      SQL
        .get(id)
        .option
        .transact(xa)
    )
    .provideService(tracing)

  override def delete(id: TodoId): Task[Unit] = Tracing
    .span(
      "delete",
      SpanKind.INTERNAL,
      errorMapper
    )(
      SQL
        .delete(id)
        .run
        .transact(xa)
        .unit
    )
    .provideService(tracing)

  override def deleteAll: Task[Unit] = Tracing
    .span(
      "deleteAll",
      SpanKind.INTERNAL,
      errorMapper
    )(
      SQL.deleteAll.run
        .transact(xa)
        .unit
    )
    .provideService(tracing)

  override def create(
      todoItemForm: TodoItemPostForm
  ): Task[TodoItem] = {
    Tracing
      .span(
        "create",
        SpanKind.INTERNAL,
        errorMapper
      )(
        SQL
          .create(todoItemForm.asTodoPayload)
          .withUniqueGeneratedKeys[Long]("ID")
          .map(id => todoItemForm.asTodoItem(TodoId(id)))
          .transact(xa)
      )
      .provideService(tracing)

  }

  override def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
  ): Task[Option[TodoItem]] =
    (for {
      oldItem <- SQL.get(id).option
      newItem = oldItem.map(_.update(todoItemForm))
      _ <- newItem.fold(connection.unit)(item => SQL.update(item).run.void)
    } yield newItem)
      .transact(xa)
}

object DoobieTodoRepository {

  def layer: ZLayer[
    Clock with AppConfig with Tracing.Service,
    Throwable,
    TodoRepository
  ] = {
    def initDb(cfg: DatabaseConfig): Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(
        cfg: DatabaseConfig
    ): ZManaged[Clock, Throwable, HikariTransactor[Task]] = {
      ZIO.runtime[Clock].toManaged.flatMap { implicit rt =>
        val dispatcherZIO: ZManaged[Any, Throwable, Dispatcher[Task]] =
          Dispatcher[Task].allocated.toManaged
            .flatMap { case (dispatcher, closeDispatcher) =>
              ZManaged.acquireRelease(ZIO(a = new Dispatcher[Task] {
                override def unsafeToFutureCancelable[A](fa: Task[A]): (Future[A], () => Future[Unit]) =
                  dispatcher.unsafeToFutureCancelable(fa)
              }))(closeDispatcher.orDie)

            }

        dispatcherZIO.flatMap { implicit dispatcher =>
          HikariTransactor
            .newHikariTransactor[Task](
              cfg.driver,
              cfg.url,
              cfg.user,
              cfg.password,
              rt.runtimeConfig.blockingExecutor.asExecutionContext
            )
            .toManaged
        }
      }
    }

    ZLayer.fromManaged {
      for {
        cfg <- ZIO.service[AppConfig].toManaged
        tracing <- ZIO.service[Tracing.Service].toManaged
        _ <- initDb(cfg.database).toManaged
        transactor <- mkTransactor(cfg.database)
      } yield new DoobieTodoRepository(transactor, tracing)
    }
  }

  object SQL {
    def create(todo: TodoPayload): Update0 = sql"""
      INSERT INTO TODOS (TITLE, COMPLETED, ORDERING)
      VALUES (${todo.title}, ${todo.completed}, ${todo.order})
      """.update

    def get(id: TodoId): Query0[TodoItem] = sql"""
      SELECT * FROM TODOS WHERE ID = ${id.value}
      """.query[TodoItem]

    val getAll: Query0[TodoItem] = sql"""
      SELECT * FROM TODOS
      """.query[TodoItem]

    def delete(id: TodoId): Update0 = sql"""
      DELETE from TODOS WHERE ID = ${id.value}
      """.update

    val deleteAll: Update0 = sql"""
      DELETE from TODOS
      """.update

    def update(todoItem: TodoItem): Update0 = sql"""
      UPDATE TODOS SET
      TITLE = ${todoItem.item.title},
      COMPLETED = ${todoItem.item.completed},
      ORDERING = ${todoItem.item.order}
      WHERE ID = ${todoItem.id.value}
      """.update
  }
}
