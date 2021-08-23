package frontend.pages.admin

import frontend.Frontend.globalContext.Node
import frontend.pages.Common
import frontend.{AdminMoviesState, FrontendState}
import levsha.dsl._
import levsha.dsl.html._
import repo.{MovieRepo, MovieRow}

import scala.concurrent.{ExecutionContext, Future}

class MoviesPage(movieRepo: MovieRepo) /*extends Page[AdminMoviesState]*/ {
  def load(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] =
    Future {
      val movies = movieRepo.list().sortBy(_.movieId)
      AdminMoviesState(movies)
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
