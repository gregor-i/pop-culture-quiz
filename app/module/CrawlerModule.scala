package module

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.google.inject.AbstractModule
import googleTranslate.TranslateQuote
import imdb.{IMDB, IMDBClient, IMDBParser}
import model.{Quote, QuoteCrawlerState, TranslatedQuote}
import model.QuoteCrawlerState.{Crawled, NotCrawled}
import play.api.{Configuration, Environment, Logger}
import play.api.inject.{Binding, Module}
import repo.{MovieRepo, MovieRow, QuoteRepo, QuoteRow}

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._


class CrawlerModule(environment: Environment, configuration: Configuration)  extends AbstractModule with ProviderSyntax {
  override def configure(): Unit = {
    bind(classOf[Starter])
      .toProvider(getProvider(classOf[Crawler]).map(new Starter(_)))
      .asEagerSingleton()
  }
}


class Starter(crawler: Crawler) {
//  crawler.run()
}

@Singleton
class Crawler @Inject()(movieRepo: MovieRepo, quoteRepo: QuoteRepo)
      (implicit as: ActorSystem, ex: ExecutionContext, mat: Materializer) {

  val crawlMovieQuotes =
  Source.repeat(())
    .throttle(1, 1.second)
    .log("imdb-crawler.trigger")
    .flatMapConcat{ _ =>
      Source(movieRepo.listUnprocessed().map(_.movieId))
    }
    .log("imdb-crawler.movieId")
    .via(
      Flow[String].mapAsyncUnordered(1){movieId =>
        IMDBClient.getMovePage(movieId).map {
          moviePage =>
            val title = IMDBParser.extractTitle(moviePage)
            val quotes = IMDBParser.extractQuotes(moviePage)
            (movieId, title, quotes)
        }
      }
    )
    .to(Sink.foreach{
      case (movieId, title, quotes) =>
        movieRepo.setState(movieId, QuoteCrawlerState.Crawled(title= title, numberOfQuotes = quotes.size,        time = ZonedDateTime.now()))
        quotes.foreach{case (quoteId, quote) => quoteRepo.addNewQuote(movieId = movieId, quoteId = quoteId, quote = quote) }
    })
    .named("imdb-crawler")

  val translateQuotes =
  Source.repeat(())
    .throttle(1, 1.second)
    .log("translator.trigger")
    .flatMapConcat{ _ =>
      Source(quoteRepo.listUnprocessed())
    }
    .log("translator.quoteId")
    .throttle(1, 1.second)
    .via(
      Flow[QuoteRow].mapAsyncUnordered[(String, TranslatedQuote)](1){ quoteRow =>
        TranslateQuote(quoteRow.quote)
          .recover(_ => TranslatedQuote(original = Quote.empty, translated = Quote.empty, chain = Seq.empty))
          .map((quoteRow.quoteId, _))
      }
    )
    .log("translator.translated")
    .to(Sink.foreach{ case (quoteId, translatedQuote) =>
      quoteRepo.setTranslatedQuote(quoteId, translatedQuote)
    })


      crawlMovieQuotes.run()
      translateQuotes.run()
}
