package todo.config

import pureconfig._
import pureconfig.generic.semiauto._

final case class HttpConfig(port: Int, baseUrl: String)
object HttpConfig {

  implicit val convert: ConfigConvert[HttpConfig] = deriveConvert
}
