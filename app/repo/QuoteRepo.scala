package repo

import play.api.db.Database

import javax.inject.{Inject, Singleton}
import anorm._
import model.Quote

import io.circe.syntax._

case class QuoteRow(quoteId: String, movieId: String, quote: Quote)

@Singleton
class QuoteRepo @Inject() (db: Database) {
  private def parser: RowParser[QuoteRow] =
    for {
      quoteId <- SqlParser.str("quote_id")
      movieId <- SqlParser.str("movie_id")
      quote <- SqlParser.str("data").collect("quote could not be decoded") {
        case QuoteData(quote) => quote
      }
    } yield QuoteRow(quoteId = quoteId, movieId = movieId, quote = quote)

  object QuoteData {
    def unapply(data: String): Option[Quote] = io.circe.parser.decode(data)(Quote.codec).toOption
  }

  def list(): Seq[QuoteRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM quotes"""
        .as(parser.*)
    }

  def addNewQuote(movieId: String, quoteId: String, quote: Quote): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO quotes (movie_id, quote_id, data)
            VALUES (${movieId}, ${quoteId}, ${quote.asJson.noSpaces})
      """
        .executeUpdate()
    }

  def delete(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""DELETE FROM movies WHERE movie_id = ${movieId}"""
        .executeUpdate()
    }
}
