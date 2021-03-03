package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import imdb.{IMDBClient, IMDBParser}
import model.{MovieData, Quote, QuoteCrawlerState, TranslatedQuote}
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
      Source(movieRepo.listNoData().map(_.movieId))
    }
    .to(
      Sink.foreach { movieId =>
        IMDBClient
          .getMoviePage(movieId)
          .map(IMDBParser.parseMoviePage)
          .map {
            case Some(movieData) => Right(movieData)
            case None            => Left("failed to parse html")
          }
          .recover(ex => Left(ex.getMessage))
          .foreach(movieRepo.setMovieData(movieId, _))
      }
    )
    .run()

  Source
    .repeat(())
    .filter(_ => running)
    .throttle(1, pollInterval)
    .flatMapConcat { _ =>
      Source(movieRepo.listNoQuotes().map(_.movieId))
    }
    .to(
      Sink.foreach { movieId =>
        IMDBClient
          .getQuotesPage(movieId)
          .map(IMDBParser.extractQuotes)
          .map(Right(_))
          .recover(ex => Left(ex.getMessage))
          .foreach(movieRepo.setQuotes(movieId, _))
      }
    )
    .run()
}
