// build.sc
import mill._

trait FooModule extends Module{
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

object bar extends FooModule{
  object inner1 extends FooModule
  object inner2 extends FooModule
}
object wrapper extends Module{
  object qux extends FooModule
}
