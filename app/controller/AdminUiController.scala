package controller

import play.api.mvc.InjectedController
import repo.{MovieRepo, MovieRow, TranslationRepo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdminUiController @Inject() (movieRepo: MovieRepo, translationRepo: TranslationRepo)(
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
      case Some(MovieRow(_, data, quotes)) =>
        Ok(views.html.admin.MovieQuotes(movieId, data, quotes))
      case None =>
        NotFound
    }
  }
}
