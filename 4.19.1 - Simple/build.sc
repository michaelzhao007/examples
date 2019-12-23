// build.sc
import mill._, scalalib._

trait SyncModule extends ScalaModule{
  def scalaVersion = "2.13.1"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:0.8.0",
    ivy"com.lihaoyi::os-lib:0.4.2",
    ivy"com.lihaoyi::os-lib-watch:0.4.2",
    ivy"com.lihaoyi::castor:0.1.1"
  )
}
object sync extends SyncModule{
  def moduleDeps = Seq(shared)
  def resources = T.sources{
    os.copy(agent.assembly().path, T.ctx().dest / "agent.jar")
    Seq(PathRef(T.ctx().dest))
  }
}
object agent extends SyncModule{
  def moduleDeps = Seq(shared)
}
object shared extends SyncModule