// agent/src/Agent.scala
package sync
object Agent {
  def main(args: Array[String]): Unit = {
    val dest = os.Path(args(0), os.pwd)
    val bytesIn = new java.io.DataInputStream(System.in)
    val bytesOut = new java.io.DataOutputStream(System.out)

    def writeResult[T: upickle.default.Writer](t: T) = {
      val result = upickle.default.writeBinary(t)
      bytesOut.writeInt(result.length)
      bytesOut.write(result)
    }

    while(true){
      val length = bytesIn.readInt()
      val buffer = new Array[Byte](length)
      bytesIn.readFully(buffer)
      upickle.default.readBinary[Msg](buffer) match{
        case Msg.IsDir(path) => writeResult(os.isDir(dest / path))
        case Msg.Exists(path) => writeResult(os.exists(dest / path))
        case Msg.ReadBytes(path) => writeResult(os.read.bytes(dest / path))
        case Msg.WriteOver(bytes, path) =>
          os.remove.all(dest / path)
          writeResult(os.write.over(dest / path, bytes, createFolders = true))
      }
    }
  }
}