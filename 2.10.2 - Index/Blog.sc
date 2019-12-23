// Blog.sc
import $ivy.`com.lihaoyi::scalatags:0.7.0`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

val postInfo = os
  .list(os.pwd / "post")
  .map(_.last.split(" - "))
  .collect{ case Array(prefix, suffix) => Some((prefix, suffix, p))}
  .sortBy(_._1.toInt)

os.write(
  os.pwd / "out" / "index.html",
  doctype("html")(
    html(
      body(
        h1("Blog"),
        for((_, suffix, _) <- postInfo)
        yield h2(suffix)
      )
    )
  )
)