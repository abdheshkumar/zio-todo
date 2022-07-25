package todo.repository

import cats.implicits._
import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import todo._
import todo.config.AppConfig
import zio._
import zio.interop.catz._
import zio.telemetry.opentelemetry.Tracing

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
      .provideLayer(ZLayer.succeed(tracing))
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
    .provideLayer(ZLayer.succeed(tracing))

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
    .provideLayer(ZLayer.succeed(tracing))

  override def deleteAll(): Task[Unit] = Tracing
    .span(
      "deleteAll",
      SpanKind.INTERNAL,
      errorMapper
    )(
      SQL.deleteAll.run
        .transact(xa)
        .unit
    )
    .provideLayer(ZLayer.succeed(tracing))

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
      .provideLayer(ZLayer.succeed(tracing))

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
    val program = for {
      cfg <- ZIO.service[AppConfig]
      tracing <- ZIO.service[Tracing.Service]
      ex <- ZIO.blockingExecutor
      transactor <- HikariTransactor
        .newHikariTransactor[Task](
          cfg.database.driver,
          cfg.database.url,
          cfg.database.user,
          cfg.database.password,
          ex.asExecutionContext
        )
        .toScopedZIO
    } yield new DoobieTodoRepository(transactor, tracing)

    ZLayer.scoped(program)
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
