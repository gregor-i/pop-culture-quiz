package repo

import anorm._
import io.circe.Json
import io.circe.syntax._
import model.{Quote, SpeechState, TranslationState}
import play.api.Mode
import play.api.db.Database

case class TranslationRow(
    movieId: String,
    quoteId: String,
    quote: Quote,
    translation: TranslationState = TranslationState.NotTranslated,
    translationService: String,
    translationChain: Seq[String],
    speech: SpeechState = SpeechState.NotProcessed
)

class TranslationRepo(db: Database, mode: Mode) extends JsonColumn {
  if (db.url.contains("amazonaws") && mode == Mode.Test)
    throw new Exception("don't run tests against production")

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
            ORDER BY (SELECT CAST(movies.quotes->translations.quote_id->>'score' AS float)
                      FROM movies
                      WHERE movies.movie_id = translations.movie_id) DESC
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
      quote              <- SqlParser.get[Option[Json]]("quote")
      translation        <- SqlParser.get[Option[Json]]("translation")
      speech             <- SqlParser.get[Option[Json]]("speech")
      translationService <- SqlParser.str("translation_service")
      translationChain   <- SqlParser.array[String]("translation_chain")
    } yield TranslationRow(
      movieId,
      quoteId,
      quote.flatMap(_.as[Quote].toOption).getOrElse(???),
      translation.flatMap(_.as[TranslationState].toOption).getOrElse(???),
      translationService,
      translationChain.toIndexedSeq,
      speech.flatMap(_.as[SpeechState].toOption).getOrElse(SpeechState.NotProcessed)
    )
}
