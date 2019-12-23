// build.sc
import mill._, scalalib._

trait SyncModule extends ScalaModule{
  def scalaVersion = "2.13.1"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::upickle:0.8.0",
    ivy"com.lihaoyi::os-lib:0.3.0"
  )
}
object sync extends SyncModule{
  def moduleDeps = Seq(shared)
}
object agent extends SyncModule{
  def moduleDeps = Seq(shared)
}
object shared extends SyncModule