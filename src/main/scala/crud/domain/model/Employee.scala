package crud.domain.model

import scala.util.Try

final case class Employee private (
    id: String,
    firstName: String,
    lastName: String
)

object Employee {

  def apply(firstName: String, lastName: String): Option[Employee] =
    Try(firstName.head.toLower.toString + lastName.substring(0, 5).toLowerCase).toOption
      .map(id => Employee(id, firstName, lastName))

}


