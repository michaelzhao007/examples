import mill._

def srcs = T.sources(millSourcePath / "src")
def resources = T.sources(millSourcePath / "resources")

def concat = T{
  os.write(
    T.ctx().dest / "concat.txt",
    for(src <- srcs(); p <- os.walk(src.path) if os.isFile(p))
      yield os.read(p)
  )
  PathRef(T.ctx().dest / "concat.txt")
}

def compress = T{
  for(res <- resources(); p <- os.walk(res.path) if os.isFile(p)){
    val copied = T.ctx().dest / p.relativeTo(res.path)
    os.copy(p, copied)
    os.proc("gzip", copied).call()
  }
  PathRef(T.ctx().dest)
}

def zipped = T{
  val temp = T.ctx().dest / "temp"
  os.makeDir(temp)
  os.copy(concat().path, temp / "concat.txt")
  for(p <- os.list(compress().path)) os.copy(p, temp / p.relativeTo(compress().path))
  os.proc("zip", "-r", T.ctx().dest / "out.zip", ".").call(cwd = temp)
  PathRef(T.ctx().dest / "out.zip")
}