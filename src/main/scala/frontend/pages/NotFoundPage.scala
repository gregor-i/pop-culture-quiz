package frontend.pages

import frontend.Frontend.globalContext.Node
import frontend.{NoRouting, NotFoundState, Page}
import levsha.dsl._
import levsha.dsl.html._

object NotFoundPage extends Page[NotFoundState] with NoRouting {
  def render(state: NotFoundState): Node = optimize {
    Html(
      Common.head("404: Not Found"),
      body(
        div(
          `class` := "section",
          article(
            `class` := "message is-danger",
            div(
              `class` := "message-body",
              div(`class` := "title", "404: NotFound."),
              a(href      := "/", "return to landing page")
            )
          )
        )
      )
    )
  }
}
