package frontend.pages

import frontend.Frontend.globalContext.Node
import levsha.dsl._
import levsha.dsl.html._
import model.{Blocking, Quote, Speech}

object Common {
  def head(title: String): Node =
    html.head(
      html.title(title),
      link(rel := "stylesheet", href := "/assets/app.css"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1"),
      link(rel := "icon", `type` := "image/svg+xml", href := "/assets/favicon.svg")
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
