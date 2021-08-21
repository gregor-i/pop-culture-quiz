package frontend

import repo.MovieRow

sealed trait FrontendState
case object AdminAgentsState                       extends FrontendState
case class AdminMoviesState(movies: Seq[MovieRow]) extends FrontendState
case class AdminMovieState(row: MovieRow)          extends FrontendState
case object NotFoundState                          extends FrontendState
case object LoadingState                           extends FrontendState
