package controller

import anorm._
import model.{Quote, TranslationState}
import play.api.db.Database
import play.api.mvc.InjectedController
import repo.{MovieRepo, MovieRow, TranslationRepo, TranslationRow}

import javax.inject.{Inject, Singleton}
@Singleton
class GameController @Inject() (db: Database) extends InjectedController {

  def play() = Action {

    val Some((movieId, quoteId, original, translation)) =
      db.withConnection { implicit con =>
        SQL"""SELECT *
              FROM translations
              ORDER BY random()
              LIMIT 10
           """
          .as(TranslationRepo.parser.*)
          .collectFirst {
            case TranslationRow(movieId, quoteId, quote, TranslationState.Translated(translated), _, _) =>
              (movieId, quoteId, quote, translated)
          }
      }

    val movie = db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE movie_id = ${movieId}"""
        .as(MovieRepo.parser.single)
    }

    val otherMovies =
      db.withConnection { implicit con =>
        SQL"""SELECT * FROM movies WHERE movie_id <> ${movie.movieId} ORDER BY random() LIMIT 3"""
          .as(MovieRepo.parser.*)
      }

    val correctTitle = movie.data match {
      case Right(movieData) => movieData.englishTitle
      case _                => "Error"
    }

    val titles = scala.util.Random.shuffle(
      otherMovies
        .collect {
          case MovieRow(_, Right(movieData), _) => movieData.englishTitle
        }
        .appended(correctTitle)
    )

    Ok(
      views.html.game.Game(
        translation = hideCharacters(translation),
        original = original,
        correctTitle = correctTitle,
        titles = titles
      )
    )
  }

  def hideCharacters(quote: Quote): Quote = {
    val transformation = quote.statements
      .flatMap(_.character)
      .distinct
      .zipWithIndex
      .map {
        case (char, index) => (char, s"Person ${index + 1}")
      }
      .toMap

    quote.copy(
      statements = quote.statements.map(statement => statement.copy(character = statement.character.map(transformation)))
    )
  }
}
