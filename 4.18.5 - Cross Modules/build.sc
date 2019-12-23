// build.sc
import mill._

val items = os.list(millSourcePath / "foo").map(_.last)
interp.watch(millSourcePath / "foo")

object foo extends Cross[FooModule](items:_*)
class FooModule(label: String) extends CrossModule{
  import mill._

  def srcs = T.sources(millSourcePath / "src")

  def concat = T{
    os.write(
      T.ctx().dest / "concat.txt",
      for(src <- srcs(); p <- os.walk(src.path) if os.isFile(p))
        yield os.read(p)
    )
    PathRef(T.ctx().dest / "concat.txt")
  }
}