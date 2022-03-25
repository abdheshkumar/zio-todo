package crud.infrastructure.persistence

import crud.domain.model.Employee
import crud.domain.repository.EmployeeRepository
import crud.domain.repository.EmployeeRepository.PersistenceFailure
import crud.domain.repository.EmployeeRepository.PersistenceFailure.UnexpectedPersistenceFailure
import zio.IO

import scala.jdk.CollectionConverters._

object EmployeeRepositoryInMemory extends EmployeeRepository {

  private val db: java.util.HashMap[String, Employee] = new java.util.HashMap()

  override def save(employee: Employee): IO[PersistenceFailure, Employee] =
    IO {
      db.put(employee.id, employee)
      employee
    }.mapError(UnexpectedPersistenceFailure)

  override def get(id: String): IO[PersistenceFailure, Option[Employee]] =
    IO(Option(db.get(id))).mapError(UnexpectedPersistenceFailure)

  override def getAll: IO[PersistenceFailure, Seq[Employee]] =
    IO(db.values().asScala.toSeq).mapError(UnexpectedPersistenceFailure)

  override def delete(id: String): IO[PersistenceFailure, Unit] =
    IO {
      db.remove(id)
      ()
    }.mapError(e => UnexpectedPersistenceFailure(e))

}
