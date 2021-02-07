package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import googleTranslate.TranslateQuote
import imdb.{IMDBClient, IMDBParser}
import model.{Quote, QuoteCrawlerState, TranslatedQuote}
import repo.{MovieRepo, QuoteRepo, QuoteRow}

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import scala.concurrent.duration._

@Singleton
class TranslationAgent @Inject()(quoteRepo: QuoteRepo)
                                (implicit as: ActorSystem, ex: ExecutionContext, mat: Materializer) extends Agent {

  var running: Boolean = false
  val pollInterval: FiniteDuration = 1.second

  Source.repeat(())
    .throttle(1, 1.second)
    .flatMapConcat{ _ =>
      Source(quoteRepo.listUnprocessed())
    }
    .throttle(1, 1.second)
    .via(
      Flow[QuoteRow].mapAsyncUnordered[(String, TranslatedQuote)](1){ quoteRow =>
        TranslateQuote(quoteRow.quote)
          .recover(_ => TranslatedQuote(original = Quote.empty, translated = Quote.empty, chain = Seq.empty))
          .map((quoteRow.quoteId, _))
      }
    )
    .to(Sink.foreach{ case (quoteId, translatedQuote) =>
      quoteRepo.setTranslatedQuote(quoteId, translatedQuote)
    })
    .run()

}
