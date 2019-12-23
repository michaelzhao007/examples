// sync/src/Sync.scala
package sync
object Sync {
  def main(args: Array[String]): Unit = {
    val src = os.Path(args(0), os.pwd)

    val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
    os.perms.set(agentExecutable, "rwx------")
    val agent = os.proc(agentExecutable, args(1)).spawn(stderr = os.Inherit)
    def callAgent[T: upickle.default.Reader](msg: Msg): () => T = {
      val bytes = upickle.default.writeBinary(msg)
      agent.stdin.writeInt(bytes.length)
      agent.stdin.write(bytes)
      agent.stdin.flush()
      () => {
        val length = agent.stdout.readInt()
        val buf = new Array[Byte](length)
        agent.stdout.readFully(buf)
        upickle.default.readBinary[T](buf)
      }
    }
    val subPaths = os.walk(src).map(_.relativeTo(src))
    def pipelineCalls[T: upickle.default.Reader](msgFor: os.RelPath => Option[Msg]) = {
      val buffer = collection.mutable.Buffer.empty[(os.RelPath, () => T)]
      for(sub <- subPaths; msg <- msgFor(sub)) buffer.append((sub, callAgent[T](msg)))
      buffer.map{case (k, v) => (k, v())}.toMap
    }

    val existsMap = pipelineCalls[Boolean](sub => Some(Msg.Exists(sub)))
    val isDirMap = pipelineCalls[Boolean](sub => Some(Msg.IsDir(sub)))

    val readMap = pipelineCalls[Array[Byte]]{sub =>
      if (existsMap(sub) && !isDirMap(sub)) Some(Msg.ReadBytes(sub))
      else None
    }

    pipelineCalls[Unit]{ sub =>
      if (os.isDir(src / sub)) None
      else{
        val localBytes = os.read.bytes(src / sub)
        if (readMap.get(sub).exists(java.util.Arrays.equals(_, localBytes))) None
        else Some(Msg.WriteOver(localBytes, sub))
      }
    }
  }
}