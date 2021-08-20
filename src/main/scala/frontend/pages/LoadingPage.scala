package frontend.pages

import levsha.dsl.html.{Html, `class`, body, fontSize, i, left, marginLeft, position, top}
import levsha.dsl._
import frontend.Frontend.globalContext.Node

object LoadingPage {
  val render: Node =
    optimize {
      Html(
        Common.head("Loading ... "),
        body(
          i(
            `class` := "fa fa-spinner fa-pulse has-text-primary",
            position @= "absolute",
            left @= "50%",
            top @= "50%",
            marginLeft @= "-5rem",
            fontSize @= "10rem"
          )
        )
      )
    }

}
