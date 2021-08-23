package frontend

import di.Agents
import model.{MovieData, Quote}
import repo.{MovieRow, TranslationRow}

sealed trait FrontendState
case class GameIndexState() extends FrontendState
case class GameQuestionState(
    releaseYearMin: Option[Int],
    releaseYearMax: Option[Int],
    readOutQuote: Boolean,
    translation: Quote,
    original: Quote,
    correctMovie: MovieData,
    movies: Seq[MovieData],
    revealed: Boolean
) extends FrontendState
case class AdminState(progress: Map[String, Int])                               extends FrontendState
case object AdminAgentsState                                                    extends FrontendState
case class AdminMoviesState(movies: Seq[MovieRow])                              extends FrontendState
case class AdminMovieState(row: MovieRow)                                       extends FrontendState
case class AdminTranslationsState(page: Int, translations: Seq[TranslationRow]) extends FrontendState
case object NotFoundState                                                       extends FrontendState
case object LoadingState                                                        extends FrontendState
