// shared/src/Shared.scala
package sync
import upickle.default.{readwriter, ReadWriter, macroRW}
sealed trait Msg
object Msg{
  implicit val subPathRW = readwriter[String].bimap[os.SubPath](_.toString, os.SubPath(_))

  implicit val msgRw: ReadWriter[Msg] = macroRW

  case class IsDir(path: os.SubPath) extends Msg
  implicit val isDirRw: ReadWriter[IsDir] = macroRW

  case class Exists(path: os.SubPath) extends Msg
  implicit val existsRw: ReadWriter[Exists] = macroRW

  case class ReadBytes(path: os.SubPath) extends Msg
  implicit val readBytesRw: ReadWriter[ReadBytes] = macroRW

  case class WriteOver(src: Array[Byte], path: os.SubPath) extends Msg
  implicit val copyOverRw: ReadWriter[WriteOver] = macroRW
}