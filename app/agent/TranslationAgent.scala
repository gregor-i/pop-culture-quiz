package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import imdb.{IMDBClient, IMDBParser}
import model.{Blocking, Quote, QuoteCrawlerState, Statement, TranslatedQuote}
import repo.{MovieRepo, QuoteRepo, QuoteRow}
import translation.google.GoogleTranslate
import translation.systran.SystranTranslate
import translation.{TranslateQuote, TranslationService}

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

abstract class TranslationAgent(quoteRepo: QuoteRepo, service: TranslationService)(
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
      Source(quoteRepo.listUnprocessed())
    }
    .throttle(1, 1.second)
    .via(
      Flow[QuoteRow].mapAsyncUnordered[(String, TranslatedQuote)](1) { quoteRow =>
        translation
          .TranslateQuote(quoteRow.quote, service = service, chain = service.chain)
          .recover{
            error =>
              running = false
              TranslatedQuote(
                original = Quote(Seq.empty, None),
                translated = Quote(Seq(Statement(None, Seq(Blocking(error.getMessage)))), None),
                chain = Seq.empty,
                service = service.name
              )
          }
          .map((quoteRow.quoteId, _))
      }
    )
    .to(Sink.foreach {
      case (quoteId, translatedQuote) =>
        quoteRepo.setTranslatedQuote(quoteId, translatedQuote)
    })
    .run()

}

@Singleton
class GoogleTranslationAgent @Inject() (quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext, mat: Materializer)
    extends TranslationAgent(quoteRepo, GoogleTranslate)

@Singleton
class SystranTranslationAgent @Inject() (quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext, mat: Materializer)
    extends TranslationAgent(quoteRepo, SystranTranslate)
