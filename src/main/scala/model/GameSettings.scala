package model

case class GameSettings(releaseYearMin: Option[Int], releaseYearMax: Option[Int], readOutQuote: Boolean)

object GameSettings {
  val default: GameSettings = GameSettings(None, None, readOutQuote = true)
}
