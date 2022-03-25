package todo.repository

import todo.{TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm}
import zio.telemetry.opentelemetry.Tracing
import zio.{Accessible, RIO}

trait TodoMessageRepository {
  def delete(id: TodoId): RIO[Tracing.Service, Unit]

  def create(
      todoItemForm: TodoItemPostForm
  ): RIO[Tracing.Service, TodoItem]

  def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
  ): RIO[Tracing.Service, Option[TodoItem]]
}

object TodoMessageRepository extends Accessible[TodoMessageRepository]
