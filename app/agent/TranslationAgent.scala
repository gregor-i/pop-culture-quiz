package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import model.TranslationState
import repo.TranslationRepo
import translation.TranslationService
import translation.google.GoogleTranslate
import translation.systran.SystranTranslate

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

abstract class TranslationAgent(service: TranslationService, translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {

  var running: Boolean             = true
  val pollInterval: FiniteDuration = 1.second

  Source
    .repeat(())
    .throttle(1, 1.second)
    .flatMapConcat { _ =>
      Source(translationRepo.listUnprocessed(service.name))
    }
    .throttle(1, 1.second)
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

@Singleton
class GoogleTranslationAgent @Inject() (translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends TranslationAgent(GoogleTranslate, translationRepo)

@Singleton
class SystranTranslationAgent @Inject() (translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends TranslationAgent(SystranTranslate, translationRepo)
