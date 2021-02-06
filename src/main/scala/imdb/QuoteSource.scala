package imdb

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import model.Quote

import scala.concurrent.ExecutionContext

object QuoteSource {
  def apply(movieId: String)(implicit as: ActorSystem, ex: ExecutionContext): Source[Quote, NotUsed] =
    Source.future(IMDB.getMovieQuotes(movieId))
      .flatMapConcat(Source.apply)
}
