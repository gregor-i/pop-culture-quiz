package dataprocessing.agent

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{KillSwitches, Materializer, UniqueKillSwitch}
import model._
import repo.{TranslationRepo, TranslationRow}
import dataprocessing.service.TextToSpeech
import org.apache.commons.net.util.Base64

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.control.NonFatal

class SpeechAgent(
    translationRepo: TranslationRepo
)(implicit
    as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {
  private val pollInterval: FiniteDuration = 5.second

  protected[agent] def startStream(): UniqueKillSwitch =
    Source
      .repeat(())
      .viaMat(KillSwitches.single)(Keep.right)
      .filter(_ => running)
      .throttle(1, pollInterval)
      .flatMapConcat { _ =>
        Source(translationRepo.listWithoutSpeech())
      }
      .throttle(1, pollInterval)
      .to(
        Sink.foreachAsync(1) {
          case TranslationRow(id, _, _, _, _, _, TranslationState.Translated(translated), _) =>
            TextToSpeech
              .toMp3Bytes(quoteToString(translated))
              .map(Base64.encodeBase64String)
              .map(SpeechState.Processed.apply)
              .recover { case NonFatal(exception) =>
                SpeechState.UnexpectedError(exception.getMessage)
              }
              .map { newState =>
                translationRepo.setSpeechState(id, newState)
              }
          case _ =>
            // todo: do something smarter to suppress the warning ...
            Future.successful(())
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
