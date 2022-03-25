package crud.domain.api

sealed trait ValidationError
object ValidationError {
  final case class InvalidInput(fistName: String, lastName: String)
    extends ValidationError
}