// FileSync.sc
@main def sync(src: os.Path, dest: os.Path) = {
  for(srcSubPath <- os.walk(src)) {
    val destSubPath = dest / srcSubPath.subRelativeTo(src)
    (os.isDir(srcSubPath), os.isDir(destSubPath)) match{
      case (false, true) | (true, false) => os.copy.over(srcSubPath, destSubPath)
      case (false, false)
        if !os.exists(destSubPath)
        || !java.util.Arrays.equals(os.read.bytes(srcSubPath), os.read.bytes(destSubPath)) =>

        os.copy.over(srcSubPath, destSubPath, createFolders = true)

      case _ => // do nothing
    }
  }
}