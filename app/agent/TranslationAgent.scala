package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import model.TranslationState
import repo.{QuoteRow, TranslationRepo, TranslationRow}
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

  var running: Boolean             = false
  val pollInterval: FiniteDuration = 1.second

  Source
    .repeat(())
    .throttle(1, 1.second)
    .flatMapConcat { _ =>
      Source(translationRepo.listUnprocessed(service.name))
    }
    .throttle(1, 1.second)
    .via(
      Flow[(TranslationRow, QuoteRow)].mapAsyncUnordered[TranslationRow](1) {
        case (translationRow, quoteRow) =>
          translation
            .TranslateQuote(quote = quoteRow.quote, service = service, chain = translationRow.translationChain)
            .transform {
              case Success(translated) => Success(TranslationState.Translated(translated))
              case Failure(exception)  => Success(TranslationState.UnexpectedError(exception.getMessage))
            }
            .map(state => translationRow.copy(translation = state))
      }
    )
    .to(Sink.foreach { translationRepo.upsert(_) })
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
