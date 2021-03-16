package controller

import io.circe.syntax._
import model.Quote
import play.api.libs.circe.Circe
import io.circe.Json
import play.api.mvc.InjectedController
import repo.QuestionService
import service.HideCharacterNames

import javax.inject.{Inject, Singleton}
import scala.util.Random

@Singleton
class GameController @Inject() (questionService: QuestionService) extends InjectedController with Circe {

  def start() = Action {
    Ok(views.html.game.Start())
  }

  def game(releaseYearMin: Int, releaseYearMax: Int) = Action {
    questionService.getOne(
      releaseYearMax = releaseYearMax,
      releaseYearMin = releaseYearMin
    ) match {
      case Some(question) =>
        Ok(
          views.html.game.Game(
            translation = HideCharacterNames(question.translatedQuote),
            original = question.originalQuote,
            correctMovie = question.correctMovie,
            movies = Random.shuffle((question.correctMovie +: question.otherMovies)),
            spokenQuoteDataUrl = question.spokenQuoteDataUrl
          )
        )
      case None =>
        NotFound
    }

  }

  def meta(releaseYearMin: Int, releaseYearMax: Int) = Action {
    Ok(
      Json.obj(
        "countMovies" ->
          Json.fromInt(
            questionService.countMovies(releaseYearMin = releaseYearMin, releaseYearMax = releaseYearMax)
          ),
        "countTranslations" ->
          Json.fromInt(questionService.countTranslations(releaseYearMin = releaseYearMin, releaseYearMax = releaseYearMax))
      )
    )
  }

  def getOne(releaseYearMin: Int, releaseYearMax: Int) = Action {
    questionService.getOne(
      releaseYearMax = releaseYearMax,
      releaseYearMin = releaseYearMin
    ) match {
      case Some(question) =>
        Ok(question.asJson)
      case None =>
        NotFound
    }
  }
}
