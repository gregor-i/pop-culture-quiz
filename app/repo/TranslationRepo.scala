package repo

import io.circe.syntax.EncoderOps
import model.{Quote, TranslationState}
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import anorm._

case class TranslationRow(
    quoteId: String,
    translation: TranslationState,
    translationService: String,
    translationChain: Seq[String]
)

@Singleton
class TranslationRepo @Inject() (db: Database) extends JsonColumn {

  def upsert(translationRow: TranslationRow): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO translations (quote_id, translation_service, translation_chain, translation)
            VALUES (${translationRow.quoteId}, ${translationRow.translationService}, ${translationRow.translationChain
        .toArray[String]}, ${translationRow.translation.asJson})
            ON CONFLICT (quote_id, translation_service, translation_chain)
            DO UPDATE SET translation = ${translationRow.translation.asJson}
         """.executeUpdate()
    }

  def insertTranslatedQuote(
      quoteId: String,
      translationService: String,
      translationChain: Seq[String],
      translation: TranslationState
  ): Int =
    upsert(
      TranslationRow(
        quoteId = quoteId,
        translationService = translationService,
        translationChain = translationChain,
        translation = translation
      )
    )

  def enqueue(quoteId: String, translationService: String, translationChain: Seq[String]): Int =
    upsert(
      TranslationRow(
        quoteId = quoteId,
        translationService = translationService,
        translationChain = translationChain,
        translation = TranslationState.NotTranslated
      )
    )

  def list(): Seq[(TranslationRow, QuoteRow)] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM translations NATURAL INNER JOIN quotes"""
        .as((TranslationRepo.parser ~ QuotesRepo.parser).*)
        .map(t => (t._1, t._2))
    }

  def listUnprocessed(service: String): Seq[(TranslationRow, QuoteRow)] =
    db.withConnection { implicit con =>
      val state: TranslationState = TranslationState.NotTranslated
      SQL"""SELECT *
            FROM translations NATURAL INNER JOIN quotes
            WHERE translation = ${state.asJson}
              AND translation_service = $service
            LIMIT 10"""
        .as((TranslationRepo.parser ~ QuotesRepo.parser).*)
        .map(t => (t._1, t._2))
    }
}

object TranslationRepo extends JsonColumn {
  def parser: RowParser[TranslationRow] =
    for {
      quoteId            <- SqlParser.str("quote_id")
      translation        <- SqlParser.get[Either[io.circe.Error, TranslationState]]("translation")
      translationService <- SqlParser.str("translation_service")
      translationChain   <- SqlParser.array[String]("translation_chain")
    } yield TranslationRow(quoteId, translation.getOrElse(???), translationService, translationChain.toIndexedSeq)
}
