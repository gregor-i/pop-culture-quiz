package repo

import anorm._
import io.circe.Json
import io.circe.syntax._
import model.{Quote, SpeechState, TranslationState}
import play.api.db.Database

case class TranslationRow(
    id: Int,
    movieId: String,
    quoteId: String,
    quote: Quote,
    translationService: String,
    translationChain: Seq[String],
    translation: TranslationState = TranslationState.NotTranslated,
    speech: SpeechState = SpeechState.NotProcessed
)

class TranslationRepo(db: Database) extends JsonColumn {
  def enqueue(movieId: String, quoteId: String, quote: Quote, translationService: String, translationChain: Seq[String]): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO translations (movie_id, quote_id, quote, translation_service, translation_chain)
            VALUES (${movieId}, ${quoteId}, ${quote.asJson}, ${translationService}, ${translationChain.toArray[String]})
            RETURNING id
        """.executeInsert(SqlParser.scalar[Int].single)
    }

  def setTranslationState(id: Integer, translationState: TranslationState): Int =
    db.withConnection { implicit con =>
      SQL"""UPDATE translations
            SET translation = ${translationState.asJson}
            WHERE id = ${id}
         """.executeUpdate()
    }

  def setSpeechState(id: Integer, speechState: SpeechState): Int =
    db.withConnection { implicit con =>
      SQL"""UPDATE translations
            SET speech = ${speechState.asJson}
            WHERE id = ${id}
         """.executeUpdate()
    }

  def getTranslation(id: Int): Option[TranslationRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM translations WHERE id = ${id}""".as(TranslationRepo.parser.singleOpt)
    }

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
            WHERE translation = ${state.asJson} OR translation IS NULL
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

  def progress(): Map[String, Int] =
    db.withConnection(implicit con =>
      SQL"""select jsonb_object_keys(translation) as key, count(*) as count
            from translations
            group by jsonb_object_keys(translation)"""
        .as((SqlParser.str(1) ~ SqlParser.int(2)).*)
        .map(t => (t._1, t._2))
        .toMap
    )
}

object TranslationRepo extends JsonColumn {
  def parser: RowParser[TranslationRow] =
    for {
      id                 <- SqlParser.int("id")
      movieId            <- SqlParser.str("movie_id")
      quoteId            <- SqlParser.str("quote_id")
      quote              <- SqlParser.get[Option[Json]]("quote")
      translation        <- SqlParser.get[Option[Json]]("translation")
      speech             <- SqlParser.get[Option[Json]]("speech")
      translationService <- SqlParser.str("translation_service")
      translationChain   <- SqlParser.array[String]("translation_chain")
    } yield TranslationRow(
      id,
      movieId,
      quoteId,
      quote.flatMap(_.as[Quote].toOption).getOrElse(???),
      translationService,
      translationChain.toIndexedSeq,
      translation.flatMap(_.as[TranslationState].toOption).getOrElse(TranslationState.NotTranslated),
      speech.flatMap(_.as[SpeechState].toOption).getOrElse(SpeechState.NotProcessed)
    )
}
