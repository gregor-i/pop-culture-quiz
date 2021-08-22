package frontend.pages.game

import di.Global
import frontend.Frontend.globalContext.{Access, Node, elementId, event}
import frontend.pages.Common
import frontend.{FrontendState, GameIndexState}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.{ExecutionContext, Future}

object IndexPage /*extends Page[GameIndexState]*/ {
  def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] =
    Future.successful(GameIndexState())

  private val releaseYearMinField = elementId()
  private val releaseYearMaxField = elementId()
  private val readOutQuoteField   = elementId()

  def render(global: Global, state: GameIndexState)(implicit ex: ExecutionContext): Node =
    optimize {
      Html(
        Common.head("Pop-Culture-Quiz"),
        body(
          form(
            `class` := "block box",
            StyleDef("max-width") @= "600px",
            margin @= "auto",
            marginTop @= "40px",
            h1(`class` := "title", "Pop-Culture-Quiz"),
            formularElement(
              "release year from:",
              input(
                `class` := "input",
                `type` := "text",
                releaseYearMinField,
                placeholder := "1995",
                min := "1900",
                max := "2025"
              )
            ),
            formularElement(
              "release year to:",
              input(
                `class` := "input",
                `type` := "text",
                releaseYearMaxField,
                placeholder := "2015",
                min := "1900",
                max := "2025"
              )
            ),
            formularElement(
              "read out quote:",
              div(
                `class` := "select",
                select(
                  readOutQuoteField,
                  option("No", value := "false"),
                  option("Yes", value := "true")
                )
              )
            ),
            div(
              `class` := "field is-horizontal",
              div(`class` := "field-label", flexGrow @= "2"),
              div(
                `class` := "field-body",
                div(
                  `class` := "control",
                  button(`class` := "button is-primary", `type` := "submit", "Play!")
                )
              )
            ),
            event("submit")(onSubmit(global))
          )
        )
      )
    }

  def onSubmit(global: Global)(access: Access)(implicit ex: ExecutionContext) =
    for {
      releaseYearMin <- access.valueOf(releaseYearMinField).map(_.toIntOption)
      releaseYearMax <- access.valueOf(releaseYearMaxField).map(_.toIntOption)
      readOutQuote <- access.valueOf(readOutQuoteField).map {
        case "true"  => true
        case "false" => false
      }
      nextState <- QuestionPage.randomQuestion(releaseYearMin, releaseYearMax, readOutQuote, global)
      _         <- access.transition(_ => nextState)
    } yield ()

  def formularElement(labelText: String, element: Node): Node =
    div(
      `class` := "field is-horizontal",
      div(`class` := "field-label is-normal", flexGrow @= "2", label(`class` := "label", labelText)),
      div(`class` := "field-body", div(`class` := "field", div(`class` := "control", element)))
    )
}
