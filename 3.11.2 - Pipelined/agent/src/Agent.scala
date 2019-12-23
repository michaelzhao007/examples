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
        case Msg.IsDir(path) => writeResult(os.isDir(os.Path(path, dest)))
        case Msg.Exists(path) => writeResult(os.exists(os.Path(path, dest)))
        case Msg.ReadBytes(path) => writeResult(os.read.bytes(os.Path(path, dest)))
        case Msg.WriteOver(bytes, path) =>
          os.remove.all(os.Path(path, dest))
          writeResult(os.write.over(os.Path(path, dest), bytes, createFolders = true))
      }
    }
  }
}