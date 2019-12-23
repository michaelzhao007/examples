// Crawler.sc
import scala.concurrent._, ExecutionContext.Implicits.global, duration._

def fetchLinks(title: String): Seq[String] = {
  val resp = requests.get(
    "https://en.wikipedia.org/w/api.php",
    params = Seq(
      "action" -> "query",
      "titles" -> title,
      "prop" -> "links",
      "format" -> "json"
    )
  )
  ujson
    .read(resp.text())("query")("pages")
    .obj
    .values
    .filter(_.obj.contains("links"))
    .flatMap(_("links").arr).map(_("title").str)
    .toSeq
}

def fetchAllLinksParallel(startTitle: String, depth: Int) = {
  var seen = Set(startTitle)
  var currentTitles = Set(startTitle)
  for(i <- 0 until depth){
    val futures = for(title <- currentTitles) yield Future{ fetchLinks(title) }
    val nextTitleLists = futures.map(Await.result(_, Duration.Inf))
    currentTitles = nextTitleLists.flatten.filter(!seen.contains(_))
    seen = seen ++ currentTitles
  }
  seen
}
// Usage
pprint.log(fetchAllLinksParallel("Singapore", 3))