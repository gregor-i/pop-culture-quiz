package controller

import play.api.mvc.InjectedController

import javax.inject.{Inject, Singleton}
import anorm._
import model.{Quote, QuoteCrawlerState, TranslationState}
import play.api.db.Database
import repo.{MovieRepo, MovieRow, QuotesRepo, TranslationRepo, TranslationRow}
@Singleton
class GameController @Inject() (db: Database) extends InjectedController {

  def play() = Action {

    val Some((quoteId, translation)) =
      db.withConnection { implicit con =>
        SQL"""SELECT *
              FROM translations
              ORDER BY random()
              LIMIT 10
           """
          .as(TranslationRepo.parser.*)
          .collectFirst {
            case TranslationRow(quoteId, TranslationState.Translated(quote), _, _) => (quoteId, quote)
          }
      }

    val original =
      db.withConnection { implicit con =>
        SQL"""SELECT * FROM quotes WHERE quote_id = ${quoteId}"""
          .as(QuotesRepo.parser.single)
      }

    val movie = db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE movie_id = ${original.movieId}"""
        .as(MovieRepo.parser.single)
    }

    val otherMovies =
      db.withConnection { implicit con =>
        SQL"""SELECT * FROM movies WHERE movie_id <> ${movie.movieId} ORDER BY random() LIMIT 3"""
          .as(MovieRepo.parser.*)
      }

    val correctTitle = movie.state match {
      case QuoteCrawlerState.Crawled(title, _, _) => title
      case _                                      => "Error"
    }

    val titles = scala.util.Random.shuffle(
      otherMovies
        .collect {
          case MovieRow(_, QuoteCrawlerState.Crawled(title, _, _)) => title
        }
        .appended(correctTitle)
    )

    Ok(
      views.html.game.Game(
        translation = hideCharacters(translation),
        original = original.quote,
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
