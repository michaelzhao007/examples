val srcRepo = scala.io.StdIn.readLine("Source repo, e.g. lihaoyi/upickle: ")
val destRepo = scala.io.StdIn.readLine("Destination repo, e.g. lihaoyi/test: ")

pprint.log(srcRepo)
pprint.log(destRepo)

val issueResponses  = {
  var done = false
  var page = 1
  val responses = collection.mutable.Buffer.empty[ujson.Value]
  while(!done){
    println("page " + page + "...")
    val resp = requests.get(
      s"https://api.github.com/repos/$srcRepo/issues?state=all&page=" + page,
      headers = Map("Authorization" -> "token 1234567890abcdef1234567890abcdef")
    )
    val parsed = ujson.read(resp.text).arr
    if (parsed.length == 0) done = true
    else responses.appendAll(parsed)
    page += 1
  }
  responses.filter(!_.obj.contains("pull_request"))
}
val issueData = for(issue <- issueResponses) yield (
  issue("number").num.toInt,
  issue("title").str,
  issue("body").str,
  issue("user")("login").str
)
val comments = {
  var done = false
  var page = 1
  val responses = collection.mutable.Buffer.empty[ujson.Value]
  while(!done){
    println("page " + page + "...")
    val resp = requests.get(
      s"https://api.github.com/repos/$srcRepo/issues/comments?page=" + page,
      headers = Map("Authorization" -> "token 1234567890abcdef1234567890abcdef")
    )
    val parsed = ujson.read(resp.text).arr
    if (parsed.length == 0) done = true
    else responses.appendAll(parsed)
    page += 1
  }
  responses
}
val commentData = for(comment <- comments) yield (
  comment("issue_url")
    .str
    .stripPrefix(s"https://api.github.com/repos/$srcRepo/issues/")
    .toInt,
  comment("user")("login").str,
  comment("body").str
)


for((number, title, body, user) <- issueData.sortBy(_._1)){
  println(s"Creating issue $number")
  val resp = requests.post(
    s"https://api.github.com/repos/$destRepo/issues",
    data = ujson.Obj(
      "title" -> title,
      "body" -> s"$body\nID: $number\nOriginal Author: $user"
    ).render(),
    headers = Map(
      "Authorization" -> "token 1234567890abcdef1234567890abcdef",
      "Content-Type" -> "application/json"
    )
  )
  println(resp.statusCode)
}

val issueNumberMapping = issueData
  .sortBy(_._1)
  .map(_._1)
  .zipWithIndex
  .toMap
  .map{case (k, v) => (k, v + 1)}

for((issueId, user, body) <- commentData; newIssueId <- issueNumberMapping.get(issueId)){
  println(s"Commenting on issue old_id=$issueId new_id=$newIssueId")
  val resp = requests.post(
    s"https://api.github.com/repos/$destRepo/issues/$newIssueId/comments",
    data = ujson.Obj("body" -> s"$body\nOriginal Author:$user").render(),
    headers = Map(
      "Authorization" -> "token 1234567890abcdef1234567890abcdef",
      "Content-Type" -> "application/json"
    )
  )
  println(resp.statusCode)
}