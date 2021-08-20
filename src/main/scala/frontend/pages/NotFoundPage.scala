package frontend.pages

import levsha.dsl.html._
import levsha.dsl._

import frontend.Frontend.globalContext.Node

object NotFoundPage {
  val render: Node = optimize {
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
              a(href := "/", "return to landing page")
            )
          )
        )
      )
    )
  }
}
