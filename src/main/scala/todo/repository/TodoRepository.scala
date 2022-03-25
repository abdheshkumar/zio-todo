package todo.repository
import todo.{TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm}
import zio._

trait TodoRepository {
  def getAll: Task[List[TodoItem]]

  def getById(id: TodoId): Task[Option[TodoItem]]

  def delete(id: TodoId): Task[Unit]

  def deleteAll: Task[Unit]

  def create(todoItemForm: TodoItemPostForm): Task[TodoItem]

  def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
  ): Task[Option[TodoItem]]
}

object TodoRepository extends Accessible[TodoRepository]
