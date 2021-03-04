package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import imdb.{IMDBClient, IMDBParser}
import repo.MovieRepo

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class IMDBAgent @Inject() (movieRepo: MovieRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {

  var running: Boolean             = true
  val pollInterval: FiniteDuration = 5.second

  Source
    .repeat(())
    .filter(_ => running)
    .throttle(1, pollInterval)
    .flatMapConcat { _ =>
      Source(movieRepo.listNoData().map(_.movieId))
    }
    .throttle(1, pollInterval)
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
    .throttle(1, pollInterval)
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
