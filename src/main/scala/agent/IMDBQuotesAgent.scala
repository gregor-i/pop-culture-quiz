package agent

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{KillSwitches, Materializer, UniqueKillSwitch}
import imdb.{IMDBClient, IMDBParser}
import repo.MovieRepo

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class IMDBQuotesAgent(movieRepo: MovieRepo)(
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
        Source(movieRepo.listNoQuotes().map(_.movieId))
      }
      .throttle(1, pollInterval)
      .to(
        Sink.foreachAsync(1) { movieId =>
          IMDBClient
            .getQuotesPage(movieId)
            .map(IMDBParser.extractQuotes)
            .map(Right(_))
            .recover(ex => Left(ex.getMessage))
            .map(movieRepo.setQuotes(movieId, _))
        }
      )
      .run()
}
