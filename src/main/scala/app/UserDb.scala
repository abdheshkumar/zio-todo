package app

import zio.{Task, ULayer, ZIO, ZLayer}

// service definition
trait UserDb {
  def insert(user: User): Task[Unit]
}

object UserDb {

  // layer - service implementation
  val liveLayer: ULayer[UserDb] = ZLayer.succeed(new UserDb {
    override def insert(user: User): Task[Unit] =
      ZIO.attempt {
        // can replace this with an actual DB SQL string
        println(s"[Database] insert into public.user values('${user.email}')")
      } *> ZIO.logInfo("Inserted.")
  })

  // Accessor Methods
  def insert(user: User): ZIO[UserDb, Throwable, Unit] =
    ZIO.serviceWithZIO(_.insert(user))
}
