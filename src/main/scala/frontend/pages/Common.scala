package frontend.pages

import frontend.Frontend.globalContext.Node
import levsha.dsl._
import levsha.dsl.html._

object Common {
  def head(title: String): Node =
    html.head(
      html.title(title),
      // todo: host bulma and font awesome myself.
      link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/bulma@0.9.1/css/bulma.min.css", crossorigin := "anonymous"),
      link(
        rel := "stylesheet",
        href := "https://pro.fontawesome.com/releases/v5.10.0/css/all.css",
        integrity := "sha384-AYmEC3Yw5cVb3ZcuHtOA93w35dYTsvhLPVnYs9eStHfGJvOvKxVfELGroGkvsg+p",
        crossorigin := "anonymous"
      ),
      meta(name := "viewport", content := "width=device-width, initial-scale=1")
    )

}
