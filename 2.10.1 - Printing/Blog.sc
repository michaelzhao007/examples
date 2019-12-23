// Blog.sc
val postInfo = os
  .list(os.pwd / "post")
  .map(_.last.split(" - "))
  .collect{ case Array(prefix, suffix) => Some((prefix, suffix, p))}
  .sortBy(_._1.toInt)

println("POSTS")
postInfo.foreach(println)