package app

import zio.ZIO

object UserSubscription {

  def subscribe(
      user: User
  ): ZIO[UserDb with Logging with UserEmailer, Throwable, Unit] =
    for {
      _ <- UserDb.insert(user)
      _ <- Logging(_.log("User saved in DB"))
      _ <- UserEmailer.sendEmail(user, "Welcome to ZIO library")
      _ <- Logging(_.log("Email sent"))
    } yield ()

}
