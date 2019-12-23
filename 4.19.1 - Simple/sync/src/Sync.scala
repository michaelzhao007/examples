// sync/src/Sync.scala
package sync
object Sync {
  def main(args: Array[String]): Unit = {
    val src = os.Path(args(0), os.pwd)

    val agentExecutable = os.temp(os.read.bytes(os.resource / "agent.jar"))
    os.perms.set(agentExecutable, "rwx------")
    val agent = os.proc(agentExecutable, args(1)).spawn(stderr = os.Inherit)

    import castor.Context.Simple.global
    sealed trait Msg
    case class ChangedPath(value: os.SubPath) extends Msg
    case class AgentResponse(value: Rpc.StatInfo) extends Msg

    object SyncActor extends castor.SimpleActor[Msg]{
      def run(msg: Msg): Unit = msg match{
        case ChangedPath(value) => Rpc.writeRpc(agent.stdin.data, Rpc.StatPath(value))
        case AgentResponse(Rpc.StatInfo(p, remoteHash)) =>
          val localHash = Rpc.hashFromPath(src / p)
          if (localHash != remoteHash && localHash.isDefined){
            Rpc.writeRpc(agent.stdin.data, Rpc.WriteOver(os.read.bytes(src / p), p))
          }
      }
    }

    val agentReader = new Thread(() => {
      while(agent.isAlive()){
        SyncActor.send(AgentResponse(Rpc.readRpc[Rpc.StatInfo](agent.stdout.data)))
      }
    })
    agentReader.start()

    os.watch.watch(
      Seq(src),
      onEvent = _.foreach(p => SyncActor.send(ChangedPath(p.subRelativeTo(src))))
    )

    Thread.sleep(999999)
  }
}
