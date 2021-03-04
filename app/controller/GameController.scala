package controller

import model.Quote
import play.api.mvc.InjectedController
import repo.QuestionService

import javax.inject.{Inject, Singleton}
import scala.util.Random

@Singleton
class GameController @Inject() (questionService: QuestionService) extends InjectedController {

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
            correctTitle = question.correctMovie.englishTitle,
            titles = Random.shuffle((question.correctMovie +: question.otherMovies).map(_.englishTitle))
          )
        )
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
