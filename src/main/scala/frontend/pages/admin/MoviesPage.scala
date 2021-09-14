package frontend.pages.admin

import frontend.Frontend.globalContext.Node
import frontend.pages.Common
import frontend.{AdminMoviesState, FrontendState, Page}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._
import repo.{MovieRepo, MovieRow}

import scala.concurrent.{ExecutionContext, Future}

class MoviesPage(movieRepo: MovieRepo)(implicit ex: ExecutionContext) extends Page[AdminMoviesState] {

  def fromState: PartialFunction[FrontendState, PathAndQuery] = { case _: AdminMoviesState =>
    Root / "admin" / "movies"
  }

  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = { case Root / "admin" / "movies" =>
    state =>
      Future {
        val movies = movieRepo.list().sortBy(_.movieId)
        AdminMoviesState(state.deviceId, movies)
      }
  }

  def render(state: AdminMoviesState): Node =
    optimize {
      Html(
        Common.head("Admin / Movies"),
        body(
          h1(`class` := "title", "Pop-Culture-Quiz Admin: Movies"),
          table(
            `class` := "table",
            thead(
              tr(
                td("Movie Id"),
                td("Title"),
                td("State")
              )
            ),
            tbody(
              seqToNode(state.movies.map(movieRow))
            )
          )

//      <form id="register-movie">
//        <input name="movieId" type="text"/>
//        <input type="submit" value="submit" onclick="submit"/>
//      </form>
        )
      )
    }

  def movieRow(movie: MovieRow): Node = {
    tr(
      td(movie.movieId),
      td(movie.data.fold[String](identity, _.englishTitle)),
      td(movie.quotes match {
        case Left(msg)     => msg
        case Right(quotes) => a(href := s"/admin/movies/${movie.movieId}", s"${quotes.size} quotes")
      })
    )
  }

}
