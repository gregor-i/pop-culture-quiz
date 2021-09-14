package frontend.pages.admin

import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{AdminMovieState, FrontendState, NotFoundState, Page}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._
import model.Quote
import repo.MovieRepo

import scala.concurrent.{ExecutionContext, Future}

class MoviePage(movieRepo: MovieRepo)(implicit ex: ExecutionContext) extends Page[AdminMovieState] {

  def fromState: PartialFunction[FrontendState, PathAndQuery] = { case state: AdminMovieState =>
    Root / "admin" / "movies" / state.row.movieId
  }

  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = {
    case Root / "admin" / "movies" / movieId =>
      state =>
        Future {
          movieRepo.get(movieId) match {
            case Some(row) => AdminMovieState(state.deviceId, row)
            case None      => NotFoundState(state.deviceId)
          }
        }
  }

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
            quotes.toSeq.sortBy(_._2.score).reverse.map { case (quoteId, quote) =>
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
