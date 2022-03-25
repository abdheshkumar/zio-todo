package crud.domain.api

sealed trait BusinessFailure
object BusinessFailure {
  final case class EmployeeAlreadyExists(id: String) extends BusinessFailure
  final case class EmployeeDoesNotExist(id: String) extends BusinessFailure
}
