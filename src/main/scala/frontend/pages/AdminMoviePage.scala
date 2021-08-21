package frontend.pages

import frontend.AdminMovieState
import frontend.Frontend.globalContext._
import levsha.dsl._
import levsha.dsl.html._
import model.Quote

object AdminMoviePage {
  def render(state: AdminMovieState): Node = {
    import state.row._
    optimize {
      Html(
        Common.head(s"Admin / Movies / ${movieId}"),
        body(
          data match {
            case Right(movieData) =>
              seqToNode(
                Seq(
                  h1(`class` := "title", s"Pop-Culture-Quiz Admin: ${movieData.englishTitle} (${movieData.originalTitle})"),
                  h2(
                    `class` := "subtitle",
                    Common.imdbLinkMovie(movieId),
                    " ",
                    movieData.releaseYear.toString,
                    " ",
                    movieData.genre.mkString(" ")
                  )
                )
              )
            case Left(msg) => h1(`class` := "title", s"Pop-Culture-Quiz Admin: ${msg}")
          },
          section(quotes match {
            case Left(msg)     => msg
            case Right(quotes) => quotesTable(movieId, quotes)
          })
        )
      )
    }
  }

  def quotesTable(movieId: String, quotes: Map[String, Quote]): Node =
    table(
      `class` := "table",
      thead(
        tr(th("Quote Id"), th("Score"), th("Quote")),
        tbody(
          seqToNode(
            quotes.toSeq.sortBy(_._2.score).reverse.map {
              case (quoteId, quote) =>
                tr(
                  td(Common.imdbQuoteLink(movieId, quoteId)),
                  td((quote.score * 100).toInt.toString, "%"),
                  td(Common.quote(quote))
                )
            }
          )
        )
      )
    )
}
