package controller

import controllers.Assets
import play.api.Environment
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo, TranslationRepo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdminUiController @Inject() (movieRepo: MovieRepo, quoteRepo: QuoteRepo, translationRepo: TranslationRepo)(
    implicit ex: ExecutionContext
) extends InjectedController {

  def movies() = Action {
    val movies = movieRepo.list().sortBy(_.movieId)
    Ok(views.html.admin.Movies(movies))
  }

  def translations() = Action {
    val translations = translationRepo.list()
    Ok(views.html.admin.Translations(translations))
  }

  def movieQuotes(movieId: String) = Action {
    movieRepo.get(movieId) match {
      case Some(movie) =>
        val quotes = quoteRepo.list().filter(_.movieId == movieId)
        Ok(views.html.admin.MovieQuotes(movie, quotes))
      case None =>
        NotFound
    }
  }
}
