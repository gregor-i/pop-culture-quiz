package repo

import anorm._
import model.SpeechState.Processed
import model.{Question, TranslationState}
import play.api.db.Database

class QuestionService(db: Database, movieRepo: MovieRepo) {

  def getOne(releaseYearMin: Option[Int], releaseYearMax: Option[Int], readOutQuote: Boolean): Option[Question] =
    for {
      TranslationRow(
        movieId,
        quoteId,
        original,
        TranslationState.Translated(translation),
        translationService,
        translationChain,
        speechState
      ) <- db
        .withConnection { implicit con =>
          SQL"""SELECT *
              FROM translations INNER JOIN movies ON translations.movie_id = movies.movie_id
              WHERE translation->>'Translated' IS NOT NULL
                AND (speech->>'Processed' IS NOT NULL OR NOT ${readOutQuote})
                AND ((movies.data->>'releaseYear') :: integer >= ${releaseYearMin} or ${releaseYearMin.isEmpty})
                AND ((movies.data->>'releaseYear') :: integer <= ${releaseYearMax} or ${releaseYearMax.isEmpty})
              ORDER BY random()
              LIMIT 1
           """
            .as(TranslationRepo.parser.singleOpt)
        }
      correctMovie <- movieRepo.get(movieId).flatMap(_.data.toOption)
      otherMovies = db
        .withConnection { implicit con =>
          SQL"""SELECT *
              FROM movies
              WHERE movie_id <> ${movieId}
                AND data->>'englishTitle' IS NOT NULL
                AND ((movies.data->>'releaseYear') :: integer >= ${releaseYearMin} or ${releaseYearMin.isEmpty})
                AND ((movies.data->>'releaseYear') :: integer <= ${releaseYearMax} or ${releaseYearMax.isEmpty})
              ORDER BY random()
              LIMIT 3"""
            .as(MovieRepo.parser.*)
        }
        .flatMap(_.data.toOption)
    } yield Question(
      originalQuote = original,
      translatedQuote = translation,
      correctMovie = correctMovie,
      otherMovies = otherMovies,
      spokenQuoteDataUrl = if (readOutQuote) Some(speechState.asInstanceOf[Processed].dataUrl) else None
    )
}
