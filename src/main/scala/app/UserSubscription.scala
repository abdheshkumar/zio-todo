package app

import zio.ZIO

object UserSubscription {

  def subscribe(
      user: User
  ): ZIO[UserDb with Logging with UserEmailer, Throwable, Unit] =
    for {
      _ <- UserDb.insert(user)
      _ <- Logging.log("User saved in DB")
      _ <- UserEmailer.sendEmail(user, "Welcome to ZIO library")
      _ <- Logging.log("Email sent")
      _ <- ZIO.logFatal("Fatal")
      _ <- ZIO.logError("Error")
      _ <- ZIO.logWarning("Warning")
      _ <- ZIO.logInfo("Info")
      _ <- ZIO.logDebug("Debug")
      _ <- ZIO.logTrace("Trace")
    } yield ()

}
