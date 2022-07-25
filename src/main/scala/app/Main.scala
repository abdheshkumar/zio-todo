package app

import zio.{Clock, Console, Scope, ZIO, ZIOAppArgs, ZLayer}

/**    Creating heavy apps involving services:
  *      - Interacting with storage layer
  *      - Business logic
  *      - Front facing API e.g through HTTP
  *      - Communicating with other services
  */
object Main extends zio.ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val user = User("Abdhesh Kumar", "abdhesh.mca@gmail.com")

    val p: ZIO[Any, Throwable, Unit] = UserSubscription
      .subscribe(user)
      .provide(
        UserEmailer.liveLayer,
        UserDb.liveLayer,
        ZLayer.succeed(Console.ConsoleLive),
        ZLayer.succeed(Clock.ClockLive),
        Logging.liveLayer
      )

    p.exitCode
  }
}
