package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import model._
import repo.{TranslationRepo, TranslationRow}
import service.{HideCharacterNames, Mp3ToDataUrl, TextToSpeech}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.NonFatal
@Singleton
class SpeechAgent @Inject() (
    translationRepo: TranslationRepo
)(
    implicit
    as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {
  var running: Boolean             = false
  val pollInterval: FiniteDuration = 5.second

  Source
    .repeat(())
    .filter(_ => running)
    .throttle(1, pollInterval)
    .flatMapConcat { _ =>
      Source(translationRepo.listWithoutSpeech())
    }
    .throttle(1, pollInterval)
    .to(
      Sink.foreachAsync(1) {
        case row @ TranslationRow(_, _, _, TranslationState.Translated(translated), _, _, _) =>
          TextToSpeech
            .toMp3Bytes(quoteToString(translated))
            .map(Mp3ToDataUrl.apply)
            .map { dataUrl =>
              row.copy(speech = SpeechState.Processed(dataUrl))
            }
            .recover {
              case NonFatal(exception) => row.copy(speech = SpeechState.UnexpectedError(exception.getMessage))
            }
            .map { row =>
              translationRepo.upsert(row)
            }
      }
    )
    .run()

  private def quoteToString(quote: Quote): String =
    quote.statements.map(statementToText).mkString("\n")

  private def statementToText(statement: Statement): String = itemsToText(statement.items)

  private def itemsToText(items: Seq[Item]): String =
    items
      .map {
        case Blocking(text) => s"[${text}]"
        case Speech(text)   => text
      }
      .mkString(" ")
}