package app
import scalatags.Text.all._
object MinimalApplication extends cask.MainRoutes{
  @cask.get("/")
  def hello() = doctype("html")(
    html(
      head(
        link(
          rel := "stylesheet",
          href := "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        )
      ),
      body(
        div(cls := "container")(
          h1("Scala Chat!"),
          hr,
          div(
            p(b("alice"), " ", "Hello World!"),
            p(b("bob"), " ", "I am cow, hear me moo"),
            p(b("charlie"), " ", "I weigh twice as much as you")
          ),
          hr,
          div(
            input(`type` := "text", placeholder := "User name",
            input(`type` := "text", placeholder := "Please write a message!", width := "100%")
          )
        )
      )
    )
  )

  initialize()
}