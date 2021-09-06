package repo

import anorm._
import dataprocessing.service.HideCharacterNames
import model.SpeechState.Processed
import model.{GameSettings, MovieData, Question, TranslationState}
import play.api.db.Database

class QuestionService(db: Database, movieRepo: MovieRepo) {

  def getOne(gameSettings: GameSettings): Option[Question] =
    for {
      translatedQuote <- pickTranslatedQuote(gameSettings)
      correctMovie    <- movieRepo.get(translatedQuote.movieId).flatMap(_.data.toOption)
      otherMovies = pickOtherOptions(translatedQuote.movieId, gameSettings)
    } yield Question(
      originalQuote = translatedQuote.quote,
      translatedQuote = HideCharacterNames(translatedQuote.translation.asInstanceOf[TranslationState.Translated].quote),
      correctMovie = correctMovie,
      otherMovies = otherMovies,
      translationId = translatedQuote.id,
      speechAvailable = translatedQuote.speech.isInstanceOf[Processed]
    )

  private def pickTranslatedQuote(gameSettings: GameSettings): Option[TranslationRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT *
              FROM translations INNER JOIN movies ON translations.movie_id = movies.movie_id
              WHERE translation->>'Translated' IS NOT NULL
                AND (speech->>'Processed' IS NOT NULL OR NOT ${gameSettings.readOutQuote})
                AND ((movies.data->>'releaseYear') :: integer >= ${gameSettings.releaseYearMin} or ${gameSettings.releaseYearMin.isEmpty})
                AND ((movies.data->>'releaseYear') :: integer <= ${gameSettings.releaseYearMax} or ${gameSettings.releaseYearMax.isEmpty})
              ORDER BY random()
              LIMIT 1
           """
        .as(TranslationRepo.parser.singleOpt)
    }

  private def pickOtherOptions(movieId: String, gameSettings: GameSettings): List[MovieData] =
    db.withConnection { implicit con =>
        SQL"""SELECT *
              FROM movies
              WHERE movie_id <> ${movieId}
                AND data->>'englishTitle' IS NOT NULL
                AND ((movies.data->>'releaseYear') :: integer >= ${gameSettings.releaseYearMin} or ${gameSettings.releaseYearMin.isEmpty})
                AND ((movies.data->>'releaseYear') :: integer <= ${gameSettings.releaseYearMax} or ${gameSettings.releaseYearMax.isEmpty})
              ORDER BY random()
              LIMIT 3"""
          .as(MovieRepo.parser.*)
      }
      .flatMap(_.data.toOption)
}
