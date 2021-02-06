package controller

import akka.actor.ActorSystem
import imdb.{IMDBClient, IMDBParser}
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DataController @Inject()(movieRepo: MovieRepo, quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext) extends InjectedController {
  def crawlMovie(movieId: String) = Action.async{
    movieRepo.get(movieId) match {
      case Some(value) => Future.successful(Accepted("Movie already crawled"))
      case None =>
        for{
          moviePage <- IMDBClient.getMovePage(movieId)
          title = IMDBParser.extractTitle(moviePage)
          _ = movieRepo.addNewMovie(movieId, title)
          quotes = IMDBParser.extractQuotes(moviePage)
          _ = quotes.foreach{
            case (quoteId, quote) => quoteRepo.addNewQuote(movieId, quoteId, quote)
          }
        } yield Accepted("Movie crawled")
    }
  }
}
