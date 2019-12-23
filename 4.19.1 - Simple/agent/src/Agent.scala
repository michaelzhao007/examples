// agent/src/Agent.scala
package sync
object Agent {
  def main(args: Array[String]): Unit = {
    val dest = os.Path(args(0), os.pwd)
    val bytesIn = new java.io.DataInputStream(System.in)
    val bytesOut = new java.io.DataOutputStream(System.out)
    while(true) try {
      Rpc.readRpc[Rpc](bytesIn) match{
        case Rpc.StatPath(path) =>
          Rpc.writeRpc(
            bytesOut,
            Rpc.StatInfo(path, Rpc.hashFromPath(dest / path))
          )

        case Rpc.WriteOver(bytes, path) =>
          os.remove.all(os.Path(path, dest))
          os.write.over(dest / path, bytes, createFolders = true)
      }
    }catch{
      case e: java.io.EOFException => return
    }
  }
}