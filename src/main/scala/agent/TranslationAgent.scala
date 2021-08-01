package agent

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{KillSwitches, Materializer, UniqueKillSwitch}
import model.TranslationState
import repo.TranslationRepo
import translation.TranslationService
import translation.google.GoogleTranslate
import translation.systran.SystranTranslate

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

abstract class TranslationAgent(service: TranslationService, translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {

  private val pollInterval: FiniteDuration = 1.second

  protected[agent] def startStream(): UniqueKillSwitch =
    Source
      .repeat(())
      .viaMat(KillSwitches.single)(Keep.right)
      .throttle(1, pollInterval)
      .flatMapConcat { _ =>
        Source(translationRepo.listWithoutTranslation(service.name))
      }
      .throttle(1, pollInterval)
      .to(
        Sink.foreachAsync(1) { translationRow =>
          translation
            .TranslateQuote(quote = translationRow.quote, service = service, chain = translationRow.translationChain)
            .map(translated => TranslationState.Translated(translated))
            .recover(exception => TranslationState.UnexpectedError(exception.getMessage))
            .map(state => translationRepo.upsert(translationRow.copy(translation = state)))
        }
      )
      .run()

}

class GoogleTranslationAgent(translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends TranslationAgent(GoogleTranslate, translationRepo)
    with Autostart

class SystranTranslationAgent(translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends TranslationAgent(SystranTranslate, translationRepo)
