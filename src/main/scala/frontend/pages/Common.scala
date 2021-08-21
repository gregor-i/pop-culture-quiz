package frontend.pages

import frontend.Frontend.globalContext.Node
import levsha.dsl._
import levsha.dsl.html._
import model.{Blocking, Quote, Speech}

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

  def quote(quote: Quote): Node =
    seqToNode(
      quote.statements.map { statement =>
        div(
          statement.character match {
            case Some(character) => b(character, ": ")
            case None            => ""
          },
          seqToNode(
            statement.items.flatMap {
              case Blocking(blocking) => Seq("[", i(blocking), "]", " ")
              case Speech(speech)     => Seq(span(speech), " ")
            }
          )
        )
      }
    )

  def imdbLinkMovie(movieId: String): Node = a(href := s"https://www.imdb.com/title/${movieId}/", movieId)

  def imdbQuoteLink(movieId: String, quoteId: String): Node =
    a(href := s"https://www.imdb.com/title/${movieId}/quotes/${quoteId}/", quoteId)
}
