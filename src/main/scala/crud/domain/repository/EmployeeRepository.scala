package crud.domain.repository

import crud.domain.model.Employee
import crud.domain.repository.EmployeeRepository.PersistenceFailure
import zio.{IO, ZIO}

trait EmployeeRepository {
  def save(employee: Employee): IO[PersistenceFailure, Employee]
  def get(id: String): IO[PersistenceFailure, Option[Employee]]
  def getAll: IO[PersistenceFailure, Seq[Employee]]
  def delete(id: String): IO[PersistenceFailure, Unit]
}

object EmployeeRepository {
  sealed trait PersistenceFailure
  object PersistenceFailure {
    final case class UnexpectedPersistenceFailure(err: Throwable)
        extends PersistenceFailure
  }

  //   accessor methods
  def save(
      employee: Employee
  ): ZIO[EmployeeRepository, PersistenceFailure, Employee] =
    ZIO.serviceWithZIO(_.save(employee))

  def get(
      id: String
  ): ZIO[EmployeeRepository, PersistenceFailure, Option[Employee]] =
    ZIO.serviceWithZIO(_.get(id))

  def getAll: ZIO[EmployeeRepository, PersistenceFailure, Seq[Employee]] =
    ZIO.serviceWithZIO(_.getAll)

  def delete(id: String): ZIO[EmployeeRepository, PersistenceFailure, Unit] =
    ZIO.serviceWithZIO(_.delete(id))
}
