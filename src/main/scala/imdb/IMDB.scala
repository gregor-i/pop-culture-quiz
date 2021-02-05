package imdb

import akka.actor.ActorSystem
import model.Quote

import scala.concurrent.{ExecutionContext, Future}

object IMDB {
  private implicit val as: ActorSystem              = ActorSystem("imdb")
  private implicit val dispatcher: ExecutionContext = as.getDispatcher

  def getMovieQuotes(movieId: String): Future[Seq[Quote]] =
    IMDBClient
      .getMovePage(movieId)
      .map(IMDBParser.parse)
}
