package controller

import controllers.Assets
import play.api.Environment
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdminUiController @Inject() (movieRepo: MovieRepo, quoteRepo: QuoteRepo)(implicit ex: ExecutionContext)
    extends InjectedController {
  def index() = Action {
    val movies = movieRepo.list().sortBy(_.movieId)
    val quotes = quoteRepo.list()
    Ok(views.html.admin.Index(movies, quotes))
  }
}
