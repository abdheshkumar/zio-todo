package crud.domain.api

import crud.domain.api.BusinessFailure.{EmployeeAlreadyExists, EmployeeDoesNotExist}
import crud.domain.api.ValidationError.InvalidInput
import crud.domain.model.Employee
import crud.domain.repository.EmployeeRepository
import crud.domain.repository.EmployeeRepository.PersistenceFailure
import zio.ZIO

object EmployeeApi {

  def create(
      req: CreateEmployeeRequest
  ): ZIO[EmployeeRepository, Any, Employee] =
    Employee(firstName = req.firstName, lastName = req.lastName) match {
      case None => ZIO.fail(InvalidInput(req.firstName, req.lastName))
      case Some(employee) =>
        EmployeeRepository.get(employee.id).flatMap {
          case Some(_) => ZIO.fail(EmployeeAlreadyExists(employee.id))
          case None    => EmployeeRepository.save(employee)
        }
    }

  def update(
      req: UpdateEmployeeRequest
  ): ZIO[EmployeeRepository, Any, Employee] =
    for {
      oldVersion <-
        if (req.firstName.isEmpty || req.lastName.isEmpty)
          ZIO.fail(InvalidInput(req.firstName, req.lastName))
        else
          EmployeeRepository
            .get(req.id)
            .flatMap {
              case Some(emp) => ZIO.succeed(emp)
              case None      => ZIO.fail(EmployeeDoesNotExist(req.id))
            }
      result <- EmployeeRepository
        .save(
          oldVersion.copy(firstName = req.firstName, lastName = req.lastName)
        )
    } yield result

  def getAll: ZIO[EmployeeRepository, PersistenceFailure, Seq[Employee]] =
    EmployeeRepository.getAll

  def get(
      id: String
  ): ZIO[EmployeeRepository, PersistenceFailure, Option[Employee]] =
    EmployeeRepository.get(id)

  def delete(id: String): ZIO[EmployeeRepository, PersistenceFailure, Unit] =
    EmployeeRepository.delete(id)

  final case class CreateEmployeeRequest(firstName: String, lastName: String)

  final case class UpdateEmployeeRequest(
      id: String,
      firstName: String,
      lastName: String
  )

}
