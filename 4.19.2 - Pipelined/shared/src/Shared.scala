// shared/src/Shared.scala
package sync
import upickle.default.{ReadWriter, macroRW, readwriter}

sealed trait Rpc
object Rpc{
  implicit val subPathRW = readwriter[String].bimap[os.SubPath](_.toString, os.SubPath(_))

  implicit val msgRw: ReadWriter[Rpc] = macroRW

  case class StatPath(path: os.SubPath) extends Rpc
  implicit val statPathRw: ReadWriter[StatPath] = macroRW

  case class WriteOver(src: Array[Byte], path: os.SubPath) extends Rpc
  implicit val WriteOverRw: ReadWriter[WriteOver] = macroRW

  case class StatInfo(p: os.SubPath, fileHash: Option[Int])

  implicit val statInfoRw: ReadWriter[StatInfo] = macroRW

  def writeRpc[T: upickle.default.Writer](out: java.io.DataOutputStream, msg: T): Unit = {
    val bytes = upickle.default.writeBinary(msg)
    out.writeInt(bytes.length)
    out.write(bytes)
    out.flush()
  }

  def readRpc[T: upickle.default.Reader](in: java.io.DataInputStream) = {
    val buf = new Array[Byte](in.readInt())
    in.readFully(buf)
    upickle.default.readBinary[T](buf)
  }

  def hashFromPath(p: os.Path) = {
    if (!os.isFile(p)) None
    else Some(java.util.Arrays.hashCode(os.read.bytes(p)))
  }
}