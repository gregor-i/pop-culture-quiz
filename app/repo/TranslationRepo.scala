package repo

import model.{Quote, SpeechState, TranslationState}
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import anorm._
import io.circe.Json
import io.circe.syntax._

case class TranslationRow(
    movieId: String,
    quoteId: String,
    quote: Quote,
    translation: TranslationState = TranslationState.NotTranslated,
    translationService: String,
    translationChain: Seq[String],
    speech: SpeechState = SpeechState.NotProcessed
)

@Singleton
class TranslationRepo @Inject() (db: Database) extends JsonColumn {

  def upsert(translationRow: TranslationRow): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO translations (movie_id, quote_id, quote, translation_service, translation_chain, translation, speech)
            VALUES (
              ${translationRow.movieId},
              ${translationRow.quoteId},
              ${translationRow.quote.asJson},
              ${translationRow.translationService},
              ${translationRow.translationChain.toArray[String]},
              ${translationRow.translation.asJson},
              ${translationRow.speech.asJson}
            )
            ON CONFLICT (quote_id, translation_service, translation_chain)
            DO UPDATE SET translation = ${translationRow.translation.asJson},
                          speech = ${translationRow.speech.asJson}
         """.executeUpdate()
    }

  def enqueue(movieId: String, quoteId: String, quote: Quote, translationService: String, translationChain: Seq[String]): Int =
    upsert(
      TranslationRow(
        movieId = movieId,
        quoteId = quoteId,
        quote = quote,
        translationService = translationService,
        translationChain = translationChain,
        translation = TranslationState.NotTranslated,
        speech = SpeechState.NotProcessed
      )
    )

  def list(offset: Int = 0, limit: Int = 100): Seq[TranslationRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM translations LIMIT ${limit} OFFSET ${offset}"""
        .as(TranslationRepo.parser.*)
    }

  def listWithoutTranslation(service: String): Seq[TranslationRow] =
    db.withConnection { implicit con =>
      val state: TranslationState = TranslationState.NotTranslated
      SQL"""SELECT *
            FROM translations
            WHERE translation = ${state.asJson}
              AND translation_service = $service
            LIMIT 10"""
        .as(TranslationRepo.parser.*)
    }

  def listWithoutSpeech(): Seq[TranslationRow] =
    db.withConnection { implicit con =>
      val state: SpeechState = SpeechState.NotProcessed
      SQL"""SELECT *
            FROM translations
            WHERE (speech = ${state.asJson} OR speech IS NULL)
              AND translation->>'Translated' IS NOT NULL
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
      speech             <- SqlParser.get[Json]("speech")
      translationService <- SqlParser.str("translation_service")
      translationChain   <- SqlParser.array[String]("translation_chain")
    } yield TranslationRow(
      movieId,
      quoteId,
      quote.as[Quote].getOrElse(???),
      translation.as[TranslationState].getOrElse(???),
      translationService,
      translationChain.toIndexedSeq,
      speech.as[SpeechState].getOrElse(SpeechState.NotProcessed)
    )
}
