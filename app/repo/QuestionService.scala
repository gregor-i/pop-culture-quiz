package repo

import anorm._
import model.{Question, TranslationState}
import play.api.db.Database

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionService @Inject() (db: Database, movieRepo: MovieRepo) {

  def getOne(releaseYearMin: Int, releaseYearMax: Int): Option[Question] =
    for {
      TranslationRow(movieId, quoteId, original, TranslationState.Translated(translation), _, _) <- db.withConnection {
        implicit con =>
          SQL"""SELECT *
              FROM translations INNER JOIN movies ON translations.movie_id = movies.movie_id
              WHERE translation->>'Translated' IS NOT NULL
                AND (movies.data->>'releaseYear') :: integer >= ${releaseYearMin}
                AND (movies.data->>'releaseYear') :: integer <= ${releaseYearMax}
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
                AND (movies.data->>'releaseYear') :: integer >= ${releaseYearMin}
                AND (movies.data->>'releaseYear') :: integer <= ${releaseYearMax}
              ORDER BY random()
              LIMIT 3"""
            .as(MovieRepo.parser.*)
        }
        .flatMap(_.data.toOption)
    } yield Question(
      originalQuote = original,
      translatedQuote = translation,
      correctMovie = correctMovie,
      otherMovies = otherMovies
    )
}
