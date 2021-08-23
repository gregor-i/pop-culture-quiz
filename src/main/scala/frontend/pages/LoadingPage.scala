package frontend.pages

import frontend.Frontend.globalContext.Node
import frontend.{LoadingState, NoRouting, Page}
import levsha.dsl._
import levsha.dsl.html._

object LoadingPage extends Page[LoadingState.type] with NoRouting {
  def render(state: LoadingState.type): Node =
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
