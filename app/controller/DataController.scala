package controller

import akka.actor.ActorSystem
import imdb.{IMDBClient, IMDBParser}
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DataController @Inject() (movieRepo: MovieRepo, quoteRepo: QuoteRepo)(implicit as: ActorSystem, ex: ExecutionContext)
    extends InjectedController {
  def registerMovie(movieId: String) = Action {
    movieRepo.get(movieId) match {
      case Some(_) =>
        movieRepo.delete(movieId)
        Accepted("Movie already registered")
      case None =>
        movieRepo.addNewMovie(movieId)
        Accepted("Movie registered and scheduled for crawling")
    }
  }
}
