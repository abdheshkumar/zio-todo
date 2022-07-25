package scheduling
import zio.{Scope, ZIO, ZIOAppArgs}

object ScheduleApp extends zio.ZIOAppDefault {

  /*
retry
retryDelay
retryDelayExponential
retryLinearAndThenExponential
   */

//Stops retrying after a specified amount of time has elapsed:

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = ???
}
