package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import imdb.{IMDBClient, IMDBParser}
import model.{Blocking, Quote, QuoteCrawlerState, Statement, TranslatedQuote}
import repo.{MovieRepo, QuoteRepo, QuoteRow}
import translation.TranslateQuote

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class TranslationAgent @Inject() (quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext, mat: Materializer)
    extends Agent {

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
          .TranslateQuote(quoteRow.quote, service = translation.systran.SystranTranslate)
          .recover(
            error =>
              TranslatedQuote(
                original = Quote(Seq.empty, None),
                translated = Quote(Seq(Statement(None, Seq(Blocking(error.getMessage)))), None),
                chain = Seq.empty,
                service = translation.systran.SystranTranslate.name
              )
          )
          .map((quoteRow.quoteId, _))
      }
    )
    .to(Sink.foreach {
      case (quoteId, translatedQuote) =>
        quoteRepo.setTranslatedQuote(quoteId, translatedQuote)
    })
    .run()

}
