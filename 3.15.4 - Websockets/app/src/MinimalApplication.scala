package app
import scalatags.Text.all._
object MinimalApplication extends cask.MainRoutes{
  case class Message(name: String, msg: String)
  import com.opentable.db.postgres.embedded.EmbeddedPostgres
  val server = EmbeddedPostgres.builder()
    .setDataDirectory("data")
    .setCleanDataDirectory(false)
    .setPort(5432)
    .start()
  import io.getquill._
  import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
  val pgDataSource = new org.postgresql.ds.PGSimpleDataSource()
  pgDataSource.setUser("postgres")
  val config = new HikariConfig()
  config.setDataSource(pgDataSource)
  val ctx = new PostgresJdbcContext(LowerCase, new HikariDataSource(config))
  ctx.executeAction("""
    CREATE TABLE IF NOT EXISTS message (
      name text,
      msg text
    );
  """)
  import ctx._
  def messages = ctx.run(query[Message].map(m => (m.name, m.msg)))

  var openConnections = Set.empty[cask.WsChannelActor]

  def messageList() = frag(for((name, msg) <- messages) yield p(b(name), " ", msg))

  @cask.postJson("/")
  def postHello(name: String, msg: String) = {
    if (name == "") ujson.Obj("success" -> false, "txt" -> "Name cannot be empty")
    else if (msg == "") ujson.Obj("success" -> false, "txt" -> "Message cannot be empty")
    else {
      ctx.run(query[Message].insert(lift(Message(name, msg))))
      val notification = cask.Ws.Text(
        ujson.Obj("index" -> messages.length, "txt" -> messageList().render).render()
      )
      for(conn <- openConnections) conn.send(notification)
      openConnections = Set.empty
      ujson.Obj("success" -> true, "txt" -> messageList().render)
    }
  }

  @cask.get("/")
  def hello() = {
    html(
      head(
        link(
          rel := "stylesheet",
          href := "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        ),
        script(raw("""
          function submitForm(){
            fetch(
              "/",
              {method: "POST", body: JSON.stringify({name: nameInput.value, msg: msgInput.value})}
            ).then(response => response.json())
             .then(json => {
              if (json.success) {
                messageList.innerHTML = json.txt
                msgInput.value = ""
                errorDiv.innerText = ""
              } else {
                errorDiv.innerText = json.txt
              }
            })
          }
          var socket = new WebSocket("ws://" + location.host + "/subscribe");
          var eventIndex = 0
          socket.onopen = function(ev){ socket.send("" + eventIndex) }
          socket.onmessage = function(ev){
            var json = JSON.parse(ev.data)
            eventIndex = json.index
            socket.send("" + eventIndex)
            messageList.innerHTML = json.txt
          }
        """))
      ),
      body(
        div(cls := "container")(
          h1("Scala Chat!"),
          hr,
          div(id := "messageList")(messageList()),
          hr,
          div(id := "errorDiv", color.red),
          form(onsubmit := "submitForm(); return false")(
            input(`type` := "text", id := "nameInput", placeholder := "User name"),
            input(`type` := "text", id := "msgInput", placeholder := "Write a message!", width := "100%"),
            input(`type` := "submit")
          )
        )
      )
    ).render
  }

  @cask.websocket("/subscribe")
  def subscribe() = cask.WsHandler { connection =>
    cask.WsActor {
      case cask.Ws.Text(msg) =>
        if (msg.toInt < messages.length){
          connection.send(
            cask.Ws.Text(ujson.Obj("index" -> messages.length, "txt" -> messageList().render).render())
          )
        }else openConnections += connection
      case cask.Ws.Close(_, _) => openConnections -= connection
    }
  }

  initialize()
}