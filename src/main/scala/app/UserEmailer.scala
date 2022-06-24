package app

import zio.{Task, ZIO, ZLayer}

// service definition
trait UserEmailer {
  def sendEmail(
      user: User,
      message: String
  ): Task[Unit] // ZIO[Any, Throwable, Unit]
}

object UserEmailer {

  // layer; includes service implementation
  val liveLayer: ZLayer[Any, Nothing, UserEmailer] =
    ZLayer.succeed(new UserEmailer {
      override def sendEmail(user: User, message: String): Task[Unit] =
        Task {
          println(s"[app.User emailer] sending '$message' to ${user.email}")
        }
    })

  // front-facing API, aka Accessor Methods
  def sendEmail(
      user: User,
      message: String
  ): ZIO[UserEmailer, Throwable, Unit] =
    ZIO.serviceWithZIO(_.sendEmail(user, message))
}
