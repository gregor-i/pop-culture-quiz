package dataprocessing.agent

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{KillSwitches, Materializer, UniqueKillSwitch}
import dataprocessing.imdb.{IMDBClient, IMDBParser}
import repo.MovieRepo

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NonFatal

class IMDBMoviePageAgent(movieRepo: MovieRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext,
    mat: Materializer
) extends Agent {

  private val pollInterval: FiniteDuration = 5.second

  protected[agent] def startStream(): UniqueKillSwitch =
    Source
      .repeat(())
      .viaMat(KillSwitches.single)(Keep.right)
      .throttle(1, pollInterval)
      .flatMapConcat { _ =>
        Source(movieRepo.listNoData().map(_.movieId))
      }
      .throttle(1, pollInterval)
      .to(
        Sink.foreachAsync(1) { movieId =>
          IMDBClient
            .getMoviePage(movieId)
            .map(IMDBParser.parseMoviePage)
            .map {
              case Some(movieData) => Right(movieData)
              case None            => Left("failed to parse html")
            }
            .recover {
              case NonFatal(ex) => Left(ex.getMessage)
            }
            .map(movieRepo.setMovieData(movieId, _))
        }
      )
      .run()
}
