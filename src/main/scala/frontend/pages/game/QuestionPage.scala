package frontend.pages.game

import dataprocessing.service.HideCharacterNames
import di.Global
import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{FrontendState, GameQuestionState, NotFoundState, Page}
import levsha.dsl._
import levsha.dsl.html._
import model.Quote

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import scala.concurrent.duration.Duration

object QuestionPage {
  def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] = {
    randomQuestion(global)
  }

  def randomQuestion(global: Global)(implicit ex: ExecutionContext) = {
    val releaseYearMax = 2030
    val releaseYearMin = 1900
    val readOutQuote   = false

    Future {
      global.repo.questionService.getOne(
        releaseYearMax = releaseYearMax,
        releaseYearMin = releaseYearMin,
        readOutQuote = readOutQuote
      ) match {
        case Some(question) =>
          GameQuestionState(
            translation = HideCharacterNames(question.translatedQuote),
            original = question.originalQuote,
            correctMovie = question.correctMovie,
            movies = Random.shuffle((question.correctMovie +: question.otherMovies)),
            revealed = false
          )

        case None =>
          NotFoundState
      }
    }
  }

  def render(state: GameQuestionState, global: Global)(implicit ex: ExecutionContext): Node = {
    import state._

    val reavealedOption = Option(revealed).filter(identity)
    optimize {
      Html(
        Common.head("Pop-Culture-Quiz"),
        body(
          `class` := "container",
          h1(`class` := "title", "Pop-Culture-Quiz"),
          quoteBlock("Scrambled:", translation),
          // todo: audio
          div(
            `class` := "buttons block options",
            seqToNode(
              movies.map(
                movie =>
                  button(
                    `class` := "button option",
                    // todo: this is buggy
                    backgroundColor @= (if (revealed && movie == correctMovie) "rgba(0, 255, 0, 0.25)" else "none"),
                    borderColor @= (if (revealed && movie == correctMovie) "green" else "none"),
                    movie.englishTitle,
                    span(`class` := "tag", movie.releaseYear.toString),
                    event("click")(_.transition(_ => state.copy(revealed = true)))
                  )
              )
            )
          ),
          reavealedOption.map(_ => quoteBlock("Original:", original)),
          reavealedOption.map(
            _ =>
              div(
                `class` := "block",
                a(
                  `class` := "button is-link",
                  "Next",
                  event("click")(
                    _.transition(_ => Await.result(randomQuestion(global), Duration.Inf))
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
