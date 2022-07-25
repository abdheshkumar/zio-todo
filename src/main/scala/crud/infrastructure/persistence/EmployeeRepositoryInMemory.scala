package crud.infrastructure.persistence

import crud.domain.model.Employee
import crud.domain.repository.EmployeeRepository
import crud.domain.repository.EmployeeRepository.PersistenceFailure
import crud.domain.repository.EmployeeRepository.PersistenceFailure.UnexpectedPersistenceFailure
import zio.ZIO
import zio.IO
import scala.jdk.CollectionConverters._

object EmployeeRepositoryInMemory extends EmployeeRepository {

  private val db: java.util.HashMap[String, Employee] = new java.util.HashMap()

  override def save(employee: Employee): IO[PersistenceFailure, Employee] =
    ZIO.attempt {
      db.put(employee.id, employee)
      employee
    }.mapError(UnexpectedPersistenceFailure)

  override def get(id: String): IO[PersistenceFailure, Option[Employee]] =
    ZIO.attempt(Option(db.get(id))).mapError(UnexpectedPersistenceFailure)

  override def getAll: IO[PersistenceFailure, Seq[Employee]] =
    ZIO.attempt(db.values().asScala.toSeq).mapError(UnexpectedPersistenceFailure)

  override def delete(id: String): IO[PersistenceFailure, Unit] =
    ZIO.attempt {
      db.remove(id)
      ()
    }.mapError(e => UnexpectedPersistenceFailure(e))

}
