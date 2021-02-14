package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import imdb.{IMDBClient, IMDBParser}
import model.{Quote, QuoteCrawlerState, TranslatedQuote}
import repo.{MovieRepo, QuoteRepo, QuoteRow}
import translation.TranslateQuote

import java.time.ZonedDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Singleton
class IMDBAgent @Inject() (movieRepo: MovieRepo, quoteRepo: QuoteRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {

  var running: Boolean             = true
  val pollInterval: FiniteDuration = 1.second

  Source
    .repeat(())
    .filter(_ => running)
    .throttle(1, pollInterval)
    .flatMapConcat { _ =>
      Source(movieRepo.listUnprocessed().map(_.movieId))
    }
    .via(
      Flow[String].mapAsyncUnordered(1) { movieId =>
        IMDBClient
          .getMovePage(movieId)
          .map { moviePage =>
            val title  = IMDBParser.extractTitle(moviePage)
            val quotes = IMDBParser.extractQuotes(moviePage)
            (movieId, title, quotes)
          }
          .transform {
            case Success((movieId, title, quotes)) =>
              Success(
                (
                  movieId,
                  QuoteCrawlerState.Crawled(title = title, numberOfQuotes = quotes.size, time = ZonedDateTime.now()),
                  quotes
                )
              )
            case Failure(exception) => Success((movieId, QuoteCrawlerState.UnexpectedError(exception.getMessage), Seq.empty))
          }
      }
    )
    .to(Sink.foreach {
      case (movieId, state, quotes) =>
        movieRepo.setState(movieId, state)
        quotes.foreach { case (quoteId, quote) => quoteRepo.addNewQuote(movieId = movieId, quoteId = quoteId, quote = quote) }
    })
    .run()
}
