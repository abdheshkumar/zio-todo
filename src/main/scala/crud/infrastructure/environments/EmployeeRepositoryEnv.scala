package crud.infrastructure.environments

import crud.domain.repository.EmployeeRepository
import crud.infrastructure.persistence.EmployeeRepositoryInMemory
import zio.{Layer, ZLayer}

object EmployeeRepositoryEnv {

  val inMemory: Layer[Nothing, EmployeeRepository] =
    ZLayer.succeed(EmployeeRepositoryInMemory)

}
