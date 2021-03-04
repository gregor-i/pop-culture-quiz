package controller

import io.circe.syntax._
import model.Quote
import play.api.libs.circe.Circe
import io.circe.Json
import play.api.mvc.InjectedController
import repo.QuestionService

import javax.inject.{Inject, Singleton}
import scala.util.Random

@Singleton
class GameController @Inject() (questionService: QuestionService) extends InjectedController with Circe {

  def start() = Action{
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
            translation = hideCharacters(question.translatedQuote),
            original = question.originalQuote,
            correctMovie = question.correctMovie,
            movies = Random.shuffle((question.correctMovie +: question.otherMovies))
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

  def hideCharacters(quote: Quote): Quote = {
    val transformation = quote.statements
      .flatMap(_.character)
      .distinct
      .zipWithIndex
      .map {
        case (char, index) => (char, s"Person ${index + 1}")
      }
      .toMap

    quote.copy(
      statements = quote.statements.map(statement => statement.copy(character = statement.character.map(transformation)))
    )
  }
}
