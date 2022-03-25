import zio.{Clock, Console, ExitCode, URIO, ZIO, ZLayer}

/**    Creating heavy apps involving services:
 *      - Interacting with storage layer
 *      - Business logic
 *      - Front facing API e.g through HTTP
 *      - Communicating with other services
 */
object Main extends zio.ZIOAppDefault {

  override def run: URIO[zio.ZEnv, ExitCode] = {
    val user = User("Abdhesh Kumar", "abdhesh.mca@gmail.com")

  val p: ZIO[Any, Throwable, Unit] =  UserSubscription
      .subscribe(user)
      .provide(
        UserEmailer.liveLayer,
        UserDb.liveLayer,
        Logging.liveLayer,
        Console.live,
        Clock.live,
        ZLayer.Debug.mermaid,
        //ZLayer.Debug.tree
      )

    p.exitCode
  }

}
/*
val layer =
      ZLayer.makeSome[Console with Clock, UserDb with Logging with UserEmailer](
        UserEmailer.liveLayer,
        UserDb.liveLayer,
        Logging.liveLayer
      )
 */
