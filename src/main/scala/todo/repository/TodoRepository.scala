package todo.repository
import todo.{TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm}
import zio._

trait TodoRepository {
  def getAll: Task[List[TodoItem]]

  def getById(id: TodoId): Task[Option[TodoItem]]

  def delete(id: TodoId): Task[Unit]

  def deleteAll(): Task[Unit]

  def create(todoItemForm: TodoItemPostForm): Task[TodoItem]

  def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
  ): Task[Option[TodoItem]]
}

object TodoRepository {
  def getAll: ZIO[TodoRepository, Throwable, List[TodoItem]] =
    ZIO.serviceWithZIO[TodoRepository](_.getAll)

  def getById(id: TodoId): ZIO[TodoRepository, Throwable, Option[TodoItem]] =
    ZIO.serviceWithZIO[TodoRepository](_.getById(id))

  def delete(id: TodoId): ZIO[TodoRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[TodoRepository](_.delete(id))

  def deleteAll(): ZIO[TodoRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[TodoRepository](_.deleteAll())

  def create(
      todoItemForm: TodoItemPostForm
  ): ZIO[TodoRepository, Throwable, TodoItem] =
    ZIO.serviceWithZIO[TodoRepository](_.create(todoItemForm))

  def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
  ): ZIO[TodoRepository, Throwable, Option[TodoItem]] =
    ZIO.serviceWithZIO[TodoRepository](_.update(id, todoItemForm))
}
