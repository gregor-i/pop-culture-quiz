package module

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import imdb.{IMDB, IMDBClient, IMDBParser}
import model.QuoteCrawlerState
import model.QuoteCrawlerState.{Crawled, NotCrawled}
import play.api.{Configuration, Environment, Logger}
import play.api.inject.{Binding, Module}
import repo.{MovieRepo, MovieRow, QuoteRepo}

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


class Starter(crawler: Crawler)

@Singleton
class Crawler @Inject()(movieRepo: MovieRepo, quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext) {
  private val logger = Logger(this.getClass)

  as.scheduler.scheduleAtFixedRate(initialDelay = 0.seconds, interval = 1.minutes)(() => run())

  def run(): Unit = {
    logger.info(s"Running Crawling")
    movieRepo.list()
      .collectFirst {case MovieRow(movieId, NotCrawled) => movieId}
      .foreach{ movieId =>
      logger.info(s"Crawling ${movieId}")
      processMovie(movieId)
        .flatMap{ state =>
          Future.successful(movieRepo.setState(movieId, state))
        }
    }
  }

  def processMovie(movieId: String): Future[QuoteCrawlerState] =
    for{
      moviePage <- IMDBClient.getMovePage(movieId)
      title = IMDBParser.extractTitle(moviePage)
      quotes = IMDBParser.extractQuotes(moviePage)
      _ = quotes.foreach{
        case (quoteId, quote) => quoteRepo.addNewQuote(movieId = movieId, quoteId = quoteId, quote = quote)
      }
    } yield QuoteCrawlerState.Crawled(
      title = title, numberOfQuotes = quotes.size, time = ZonedDateTime.now()
    )
}
