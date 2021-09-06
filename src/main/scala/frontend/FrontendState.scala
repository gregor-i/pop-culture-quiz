package frontend

import model.{GameSettings, Question}
import repo.{MovieRow, TranslationRow}

sealed trait FrontendState {
  def deviceId: String
}
case class GameIndexState(deviceId: String)        extends FrontendState
case class RoomState(deviceId: String, id: String) extends FrontendState
case class GameQuestionState(
    deviceId: String,
    gameSettings: GameSettings,
    question: Question,
    revealed: Boolean
) extends FrontendState
case class AdminState(deviceId: String, progress: Map[String, Int])                               extends FrontendState
case class AdminAgentsState(deviceId: String)                                                     extends FrontendState
case class AdminMoviesState(deviceId: String, movies: Seq[MovieRow])                              extends FrontendState
case class AdminMovieState(deviceId: String, row: MovieRow)                                       extends FrontendState
case class AdminTranslationsState(deviceId: String, page: Int, translations: Seq[TranslationRow]) extends FrontendState
case class NotFoundState(deviceId: String)                                                        extends FrontendState
case class LoadingState(deviceId: String)                                                         extends FrontendState
