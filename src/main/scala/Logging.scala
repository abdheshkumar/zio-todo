import zio._

trait Logging {
  def log(line: String): UIO[Unit]
}

object Logging extends Accessible[Logging] {

  val liveLayer: ZLayer[Console with Clock, Nothing, Logging] = ZLayer(for {
    clock <- ZIO.service[Clock]
    console <- ZIO.service[Console]
  } yield new Logging {
    override def log(line: String): UIO[Unit] =
      for {
        now <- clock.localDateTime
        _ <- console.printLine(s"$now-$line").ignore
      } yield ()
  })
}
