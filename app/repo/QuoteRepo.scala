package repo

import akka.Done
import akka.stream.scaladsl.Sink
import anorm._
import io.circe.syntax._
import model.{Quote, TranslatedQuote}
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

case class QuoteRow(quoteId: String, movieId: String, quote: Quote, translated: Option[TranslatedQuote])

@Singleton
class QuoteRepo @Inject() (db: Database) extends JsonColumn {
  private def parser: RowParser[QuoteRow] =
    for {
      quoteId <- SqlParser.str("quote_id")
      movieId <- SqlParser.str("movie_id")
      quote <- SqlParser.get[Either[io.circe.Error, Quote]]("data")
      translatedQuote <- SqlParser.get[Either[io.circe.Error, TranslatedQuote]]("translated_quote").?
    } yield QuoteRow(
      quoteId = quoteId,
      movieId = movieId,
      quote = quote.getOrElse(???),
      translated = translatedQuote.flatMap(_.toOption)
    )

  def list(): Seq[QuoteRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM quotes"""
        .as(parser.*)
    }

  def listUnprocessed(): Seq[QuoteRow] =
    db.withConnection{implicit con =>
      SQL"""SELECT * FROM quotes WHERE translated_quote IS NULL LIMIT 10"""
        .as(parser.*)
    }

  def addNewQuote(movieId: String, quoteId: String, quote: Quote): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO quotes (movie_id, quote_id, data)
            VALUES (${movieId}, ${quoteId}, ${quote.asJson})
      """
        .executeUpdate()
    }

  def addNewQuote(quoteRow: QuoteRow): Int =
    addNewQuote(movieId = quoteRow.movieId, quoteId = quoteRow.quoteId, quote = quoteRow.quote)

  def setTranslatedQuote(quoteId: String, translated: TranslatedQuote): Int =
    db.withConnection{ implicit con =>
      SQL"""UPDATE quotes
            SET translated_quote = ${translated.asJson}
            WHERE quote_id = ${quoteId}
         """
        .executeUpdate()
    }

  def delete(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""DELETE FROM movies WHERE movie_id = ${movieId}"""
        .executeUpdate()
    }
}
