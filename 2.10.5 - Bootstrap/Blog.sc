// Blog.sc
import $ivy.`com.lihaoyi::scalatags:0.7.0`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.5.1`

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

os.remove.all(os.pwd / "out" / "post")
os.makeDir.all(os.pwd / "out" / "post")
for((_, suffix, path) <- postInfo) {
  val parser = org.commonmark.parser.Parser.builder().build()
  val document = parser.parse(os.read(path))
  val renderer = org.commonmark.html.HtmlRenderer.builder().build()
  val output = renderer.render(document)
  os.write(
    os.pwd / "out" / "post" / mdNameToHtml(suffix),
    doctype("html")(
      html(
        head(bootstrapCss),
        body(
          h1(a("Blog", href := "../index.html"), " / ", suffix.stripSuffix(".md")),
          raw(output)
        )
      )
    )
  )
}

os.write(
  os.pwd / "out" / "index.html",
  doctype("html")(
    html(
      head(bootstrapCss),
      body(
        h1("Blog"),
        for((_, suffix, _) <- postInfo)
        yield h2(a(suffix, href := ("post/" + mdNameToHtml(suffix))))
      )
    )
  )
)