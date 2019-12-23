// build.sc
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