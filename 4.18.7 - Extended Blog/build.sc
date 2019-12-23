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

def bootstrap = T{
  os.write(
    T.ctx().dest / "bootstrap.min.css",
    requests.get("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css").text()
  )
  PathRef(T.ctx().dest / "bootstrap.min.css")
}

object post extends Cross[PostModule](postInfo.map(_._1):_*)
class PostModule(number: String) extends Module{
  val Some((_, suffix, path)) = postInfo.find(_._1 == number)
  def srcPath = T.sources(path)
  def renderMarkdown(s: String) = {
    val parser = org.commonmark.parser.Parser.builder().build()

    val document = parser.parse(s)
    val renderer = org.commonmark.html.HtmlRenderer.builder().build()
    renderer.render(document)
  }
  def preview = T{
    val Seq(src) = srcPath()
    val firstPara = os.read.lines(src.path).takeWhile(_.nonEmpty)
    renderMarkdown(firstPara.mkString("\n"))
  }
  def render = T{
    val Seq(src) = srcPath()
    val output = renderMarkdown(os.read(src.path))
    os.write(
      T.ctx().dest /  mdNameToHtml(suffix),
      html(
        head(head(link(rel := "stylesheet", href := "../bootstrap.min.css"))),
        body(
          h1(a("Blog", href := "../index.html"), " / ", suffix.stripSuffix(".md")),
          raw(output)
        )
      ).render
    )
    PathRef(T.ctx().dest / mdNameToHtml(suffix))
  }
}

val previews = mill.define.Task.sequence(post.itemMap.values.map(_.preview).toSeq)

def links = T.input(postInfo.map(_._2))

def index = T{
  os.write(
    T.ctx().dest / "index.html",
    html(
      head(head(link(rel := "stylesheet", href := "bootstrap.min.css"))),
      body(
        h1("Haoyi's Blog"),
        for ((suffix, preview) <- links().zip(previews()))
          yield frag(
            h2(a(suffix, href := ("post/" + mdNameToHtml(suffix)))),
            raw(preview)
          )
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

  os.copy(bootstrap().path, T.ctx().dest / "bootstrap.min.css")
  os.copy(index().path, T.ctx().dest / "index.html")

  PathRef(T.ctx().dest)
}