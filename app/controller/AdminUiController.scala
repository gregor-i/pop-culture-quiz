package controller

import controllers.Assets
import model.Quote
import play.api.Environment
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo, QuoteRow, TranslationRepo}

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
        val quotes = quoteRepo.list().filter(_.movieId == movieId).sortBy{
          case QuoteRow(_, _, Quote(_, Some((pos, n)))) => 1 - Score.score(pos, n)
          case _ => 1
        }

        Ok(views.html.admin.MovieQuotes(movie, quotes))
      case None =>
        NotFound
    }
  }
}


object Score{
  // source:  https://www.evanmiller.org/how-not-to-sort-by-average-rating.html
  def score(pos: Int, n: Int): Double = {
    // note: this is a modification from the original algorithms.
    // pretending to start with a single like, makes dislikes count. ie: score(0, 1) < score(0, 0)
    val z    = 1.96
    val phat = pos.toDouble / n
    (phat + z * z / (2.0 * n) - z * Math.sqrt((phat * (1.0 - phat) + z * z / (4.0 * n)) / n)) / (1.0 + z * z / n)
  }
}
