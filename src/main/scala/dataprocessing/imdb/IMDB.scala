package dataprocessing.imdb

import akka.actor.ActorSystem
import model.Quote

import scala.concurrent.{ExecutionContext, Future}

object IMDB {
  def getMovieQuotes(movieId: String)(implicit as: ActorSystem, ex: ExecutionContext): Future[Map[String, Quote]] =
    IMDBClient
      .getQuotesPage(movieId)
      .map(IMDBParser.extractQuotes)
}
