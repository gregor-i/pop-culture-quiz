package frontend.pages.game

import di.Global
import frontend.Frontend.globalContext.Node
import frontend.pages.Common
import frontend.{FrontendState, GameIndexState, Page}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.{ExecutionContext, Future}

object IndexPage extends Page[GameIndexState] {
  override def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] =
    Future.successful(GameIndexState())

  override def render(state: GameIndexState): Node =
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
              input(`class` := "input", `type` := "text", name := "releaseYearMin", placeholder := "1995")
            ),
            formularElement(
              "release year to:",
              input(`class` := "input", `type` := "text", name := "releaseYearMax", placeholder := "2015")
            ),
            div(
              `class` := "field is-horizontal",
              div(`class` := "field-label", flexGrow @= "2", label(`class` := "label", "read out quote:")),
              div(
                `class` := "field-body",
                div(
                  `class` := "field",
                  div(
                    `class` := "control",
                    label(`class` := "radio", input(`type` := "radio", name := "readOutQuote", value := "true"), " Yes "),
                    label(
                      `class` := "radio",
                      input(`type` := "radio", name := "readOutQuote", value := "false", checked),
                      " No "
                    )
                  )
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
                  input(`class` := "button is-primary", `type` := "submit", value := "Play!")
                )
              )
            )
          )
        )
      )
    }

  def formularElement(labelText: String, element: Node): Node =
    div(
      `class` := "field is-horizontal",
      div(`class` := "field-label is-normal", flexGrow @= "2", label(`class` := "label", labelText)),
      div(`class` := "field-body", div(`class` := "field", div(`class` := "control", element)))
    )
}
