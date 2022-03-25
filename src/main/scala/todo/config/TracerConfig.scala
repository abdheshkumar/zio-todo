package todo.config
import pureconfig._
import pureconfig.generic.semiauto._

final case class TracerConfig(host: String)

object TracerConfig {
  implicit val convert: ConfigConvert[TracerConfig] = deriveConvert
}
