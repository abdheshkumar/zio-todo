package crud

import crud.domain.repository.EmployeeRepository
import crud.infrastructure.CRUDOperation._
import crud.infrastructure.environments.EmployeeRepositoryEnv
import crud.infrastructure.{CRUDOperation, Controller}
import zio.Console.{printLine, readLine}
import zio.{Console, ZEnv, ZIO, ZIOAppArgs}

import java.io.IOException
import scala.util.Try

object Application extends zio.ZIOAppDefault {

  type ApplicationEnvironment = Console with EmployeeRepository

  private val localApplicationEnvironment =
    Console.live ++ EmployeeRepositoryEnv.inMemory

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = {
    for {
      args <- ZIOAppArgs.getArgs
      profile = Try(args.head).getOrElse("")
      _ <-
        if (profile == "local")
          program().provideLayer(localApplicationEnvironment)
        else printLine(s"Unsupported profile $profile").orDie *> ZIO.succeed(1)

    } yield ()

  }

  def help(): ZIO[Console, IOException, Unit] =
    for {
      _ <- printLine("Please select next operation to perform:")
      _ <- printLine(s"${Create.index} for Create") *> printLine(
        s"${Read.index} for Read"
      ) *> printLine(
        s"${Update.index} for Update"
      ) *> printLine(s"${Delete.index} for Delete") *> printLine(
        s"${GetAll.index} for GetAll"
      ) *> printLine(
        s"${ExitApp.index} for ExitApp"
      )
    } yield ()

  def program(): ZIO[Console with EmployeeRepository, Nothing, Int] =
    (for {
      _ <- help()
      selection <- readLine
      _ <- CRUDOperation.selectOperation(selection) match {
        case Some(op) =>
          op match {
            case ExitApp => printLine(s"Shutting down")
            case _ =>
              dispatch(op)
                .tapError(e => printLine(s"Failed with: $e"))
                .flatMap(s =>
                  printLine(s"Succeeded with $s") *> program()
                ) orElse program()
          }
        case None =>
          printLine(
            s"$selection is not a valid selection, please try again!"
          ) *> program()
      }
    } yield 0).tapError(e => printLine(s"Unexpected Failure $e")) orElse ZIO
      .succeed(1)

  def dispatch(
      operation: CRUDOperation
  ): ZIO[Console with EmployeeRepository, Any, Any] = operation match {
    case Create  => Controller.create
    case Read    => Controller.read
    case Update  => Controller.update
    case Delete  => Controller.delete
    case GetAll  => Controller.getAll
    case ExitApp => ZIO.fail("How did I get here?")
  }

}
