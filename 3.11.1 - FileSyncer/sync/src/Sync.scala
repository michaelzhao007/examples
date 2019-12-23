// sync/src/Sync.scala
package sync
object Sync {
  def main(args: Array[String]): Unit = {
    val src = os.Path(args(0), os.pwd)
    val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
    os.perms.set(agentExecutable, "rwx------")
    val agent = os.proc(agentExecutable, args(1)).spawn(stderr = os.Inherit)
    def callAgent[T: upickle.default.Reader](msg: Msg): T = {
      val bytes = upickle.default.writeBinary(msg)
      agent.stdin.writeInt(bytes.length)
      agent.stdin.write(bytes)
      agent.stdin.flush()
      val length = agent.stdout.readInt()
      val buf = new Array[Byte](length)
      agent.stdout.readFully(buf)
      upickle.default.readBinary[T](buf)
    }

    val srcContents = os.walk(src)
    for(srcSubPath <- srcContents) {
      val subPath = srcSubPath.subRelativeTo(src)
      val destSubPath = dest / subPath
      (os.isDir(srcSubPath), callAgent[Boolean](Msg.IsDir(subPath))) match{
        case (false, true) =>
          callAgent[Unit](Msg.WriteOver(os.read.bytes(srcSubPath), subPath))
        case (true, false) =>
          for(p <- os.walk(srcSubPath) if os.isFile(p)){
            callAgent[Unit](Msg.WriteOver(os.read.bytes(p), p.relativeTo(dest)))
          }
        case (false, false)
          if !callAgent[Boolean](Msg.Exists(subPath))
            || java.util.Arrays.equals(os.read.bytes(srcSubPath), callAgent[Array[Byte]](Msg.ReadBytes(relPath))) =>
          callAgent[Unit](Msg.WriteOver(os.read.bytes(srcSubPath), subPath))
        case _ => // do nothing
      }
    }
  }
}