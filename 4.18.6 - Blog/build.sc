// build.sc
import $ivy.`com.lihaoyi::scalatags:0.7.0`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`
import mill._

interp.watch(os.pwd / "post")
val postInfo = os
  .list(os.pwd / "post")
  .map(_.last.split(" - "))
  .collect{ case Array(prefix, suffix) => Some((prefix, suffix, p))}
  .sortBy(_._1.toInt)

def mdNameToHtml(name: String) = name.stripSuffix(".md").replace(" ", "-").toLowerCase + ".html"

val bootstrapCss = link(
  rel := "stylesheet",
  href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
)

object post extends Cross[PostModule](postInfo.map(_._1):_*)
class PostModule(number: String) extends Module{
  val Some((_, suffix, path)) = postInfo.find(_._1 == number)
  def srcPath = T.sources(path)
  def render = T{
    val Seq(src) = srcPath()
    val parser = org.commonmark.parser.Parser.builder().build()
    val document = parser.parse(os.read(src.path))
    val renderer = org.commonmark.html.HtmlRenderer.builder().build()
    val output = renderer.render(document)
    os.write(
      T.ctx().dest /  mdNameToHtml(suffix),
      html(
        head(bootstrapCss),
        body(
          h1(a("Blog", href := "../index.html"), " / ", suffix.stripSuffix(".md")),
          raw(output)
        )
      ).render
    )
    PathRef(T.ctx().dest / mdNameToHtml(suffix))
  }
}

def links = T.input(postInfo.map(_._2))

def index = T{
  os.write(
    T.ctx().dest / "index.html",
    html(
      head(bootstrapCss),
      body(
        h1("Blog"),
        for (suffix <- links())
          yield h2(a(suffix, href := ("post/" + mdNameToHtml(suffix))))
      )
    ).render
  )

  PathRef(T.ctx().dest / "index.html")
}

val posts = mill.define.Task.sequence(postInfo.map(_._1).map(post(_).render))

def dist = T {
  for (post <- posts()) {
    os.copy(post.path, T.ctx().dest / "post" / post.path.last, createFolders = true)
  }
  os.copy(index().path, T.ctx().dest / "index.html")

  PathRef(T.ctx().dest)
}