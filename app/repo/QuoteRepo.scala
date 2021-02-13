package repo

import anorm._
import io.circe.syntax._
import model.Quote
import play.api.db.Database

import javax.inject.{Inject, Singleton}

case class QuoteRow(quoteId: String, movieId: String, quote: Quote)

@Singleton
class QuoteRepo @Inject() (db: Database) extends JsonColumn {
  def list(): Seq[QuoteRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM quotes"""
        .as(QuotesRepo.parser.*)
    }

  def find(quoteId: String): Option[QuoteRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM quotes WHERE quote_id = ${quoteId}"""
        .as(QuotesRepo.parser.singleOpt)
    }

  def addNewQuote(movieId: String, quoteId: String, quote: Quote): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO quotes (movie_id, quote_id, data)
            VALUES (${movieId}, ${quoteId}, ${quote.asJson})
            ON CONFLICT (quote_id)
            DO UPDATE SET data = ${quote.asJson}
      """
        .executeUpdate()
    }

  def addNewQuote(quoteRow: QuoteRow): Int =
    addNewQuote(movieId = quoteRow.movieId, quoteId = quoteRow.quoteId, quote = quoteRow.quote)
}

object QuotesRepo extends JsonColumn {
  def parser: RowParser[QuoteRow] =
    for {
      quoteId <- SqlParser.str("quote_id")
      movieId <- SqlParser.str("movie_id")
      quote   <- SqlParser.get[Either[io.circe.Error, Quote]]("data")
    } yield QuoteRow(
      quoteId = quoteId,
      movieId = movieId,
      quote = quote.getOrElse(???)
    )
}
