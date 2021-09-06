package frontend.pages.game

import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{FrontendState, GameQuestionState, NotFoundState, Page}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._
import model.{GameSettings, Quote}
import repo.QuestionService

import scala.concurrent.{ExecutionContext, Future}

class QuestionPage(questionService: QuestionService)(implicit ex: ExecutionContext) extends Page[GameQuestionState] {

  def fromState: PartialFunction[FrontendState, PathAndQuery] = {
    case _: GameQuestionState => Root / "game"
  }

  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = {
    case Root / "game" => state => randomQuestion(state.deviceId, GameSettings.default)
  }

  def randomQuestion(deviceId: String, gameSettings: GameSettings) =
    Future {
      questionService.getOne(gameSettings) match {
        case Some(question) =>
          GameQuestionState(
            deviceId = deviceId,
            gameSettings = gameSettings,
            question = question,
            revealed = false
          )

        case None =>
          NotFoundState(deviceId)
      }
    }

  def render(state: GameQuestionState): Node = {
    import state._

    val reavealedOption = Option(revealed).filter(identity)
    optimize {
      Html(
        Common.head("Pop-Culture-Quiz"),
        body(
          `class` := "container",
          h1(`class` := "title", "Pop-Culture-Quiz"),
          quoteBlock("Scrambled:", question.translatedQuote),
          Option(state.question.speechAvailable).filter(identity).filter(_ => !state.revealed).map { _ =>
            div(
              `class` := "audio",
              audio(
                if (state.gameSettings.readOutQuote)
                  autoplay := "autoplay"
                else
                  autoplay := "null",
                controls := "controls",
                source(
                  src := s"/api/translations/${state.question.translationId}/speech"
                )
              )
            )
          },
          div(
            `class` := "buttons block options",
            seqToNode(
              question.movies.map(
                movie =>
                  button(
                    `class` := s"button option ${if (revealed && movie == question.correctMovie) "correct-answer" else ""}",
                    movie.englishTitle,
                    span(`class` := "tag", movie.releaseYear.toString),
                    event("click")(_.transition(_ => state.copy(revealed = true)))
                  )
              )
            )
          ),
          reavealedOption.map(_ => quoteBlock("Original:", question.originalQuote)),
          reavealedOption.map(
            _ =>
              div(
                `class` := "block",
                a(
                  `class` := "button is-link",
                  "Next",
                  event("click")(
                    access =>
                      randomQuestion(deviceId, gameSettings)
                        .flatMap(nextState => access.transition(_ => nextState))
                  )
                )
              )
          )
        )
      )
    }
  }

  def quoteBlock(title: String, quote: Quote): Node =
    div(`class` := "block", h3(`class` := "title", title), Common.quote(quote))
}

//  <style>
//  body .original{
//  display: none;
//  }
//
//  body.revealed .original {
//  display: block;
//  }
//
//  body.revealed button.correct {
//  border-color: green;
//  background: rgba(0, 255, 0, 0.25);
//  }
//
//  body .next-link {
//  display: none;
//  }
//
//  body.revealed .next-link {
//  display: block;
//  }
//
//  .options {
//  display: flex;
//  flex-wrap: wrap;
//  }
//
//  .options .option {
//  display: block;
//  flex-basis: 34%;
//  flex-grow: 1;
//  margin: .5rem;
//  }
//
//  .audio {
//  display: flex;
//  justify-content: center;
//  }
//  </style>
