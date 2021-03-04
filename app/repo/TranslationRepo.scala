package repo

import model.{Quote, TranslationState}
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import anorm._
import io.circe.Json
import io.circe.syntax._

case class TranslationRow(
    movieId: String,
    quoteId: String,
    quote: Quote,
    translation: TranslationState,
    translationService: String,
    translationChain: Seq[String]
)

@Singleton
class TranslationRepo @Inject() (db: Database) extends JsonColumn {

  def upsert(translationRow: TranslationRow): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO translations (movie_id, quote_id, quote, translation_service, translation_chain, translation)
            VALUES (
              ${translationRow.movieId},
              ${translationRow.quoteId},
              ${translationRow.quote.asJson},
              ${translationRow.translationService},
              ${translationRow.translationChain.toArray[String]},
              ${translationRow.translation.asJson}
            )
            ON CONFLICT (quote_id, translation_service, translation_chain)
            DO UPDATE SET translation = ${translationRow.translation.asJson}
         """.executeUpdate()
    }

  def insertTranslatedQuote(
      movieId: String,
      quoteId: String,
      quote: Quote,
      translationService: String,
      translationChain: Seq[String],
      translation: TranslationState
  ): Int =
    upsert(
      TranslationRow(
        movieId = movieId,
        quoteId = quoteId,
        quote = quote,
        translationService = translationService,
        translationChain = translationChain,
        translation = translation
      )
    )

  def enqueue(movieId: String, quoteId: String, quote: Quote, translationService: String, translationChain: Seq[String]): Int =
    upsert(
      TranslationRow(
        movieId = movieId,
        quoteId = quoteId,
        quote = quote,
        translationService = translationService,
        translationChain = translationChain,
        translation = TranslationState.NotTranslated
      )
    )

  def list(): Seq[TranslationRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM translations"""
        .as(TranslationRepo.parser.*)
    }

  def listUnprocessed(service: String): Seq[TranslationRow] =
    db.withConnection { implicit con =>
      val state: TranslationState = TranslationState.NotTranslated
      SQL"""SELECT *
            FROM translations
            WHERE translation = ${state.asJson}
              AND translation_service = $service
            LIMIT 10"""
        .as(TranslationRepo.parser.*)
    }
}

object TranslationRepo extends JsonColumn {
  def parser: RowParser[TranslationRow] =
    for {
      movieId            <- SqlParser.str("movie_id")
      quoteId            <- SqlParser.str("quote_id")
      quote              <- SqlParser.get[Json]("quote")
      translation        <- SqlParser.get[Json]("translation")
      translationService <- SqlParser.str("translation_service")
      translationChain   <- SqlParser.array[String]("translation_chain")
    } yield TranslationRow(
      movieId,
      quoteId,
      quote.as[Quote].getOrElse(???),
      translation.as[TranslationState].getOrElse(???),
      translationService,
      translationChain.toIndexedSeq
    )
}
