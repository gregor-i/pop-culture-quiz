package frontend.pages

import di.Global
import frontend.{AdminTranslationsState, FrontendState, Page}
import frontend.Frontend.globalContext._
import levsha.dsl._
import levsha.dsl.html._
import model.TranslationState
import repo.TranslationRow

import scala.concurrent.{ExecutionContext, Future}

object AdminTranslationsPage /*extends Page[AdminTranslationsState]*/ {

  def load(global: Global, pageQP: Option[String])(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] =
    Future {
      val pageNumber   = pageQP.flatMap(_.toIntOption).getOrElse(1)
      val translations = global.repo.translationRepo.list(offset = (pageNumber - 1) * 100)
      AdminTranslationsState(pageNumber, translations)
    }

  def render(state: AdminTranslationsState): Node =
    optimize {
      Html(
        Common.head("Admin / Translations"),
        body(
          h1(`class` := "title", s"Pop-Culture-Quiz Admin / Translations"),
          table(
            `class` := "table",
            thead(
              tr(
                th(div("Movie Id"), div("Quote Id")),
                th(div("Translation Service"), div("Translation Chain")),
                th("Original"),
                th("Translation")
              )
            ),
            tbody(
              seqToNode(state.translations.map(translationRow))
            )
          )
        )
      )
    }

  def translationRow(translation: TranslationRow): Node =
    tr(
      td(
        div(translation.movieId),
        div(translation.quoteId)
      ),
      td(
        div(translation.translationService),
        div(translation.translationChain.mkString(", "))
      ),
      td(Common.quote(translation.quote)),
      td(
        translation.translation match {
          case TranslationState.NotTranslated            => "awaiting translation"
          case TranslationState.Translated(quote)        => Common.quote(quote)
          case TranslationState.UnexpectedError(message) => s"Error: ${message}"
        }
      )
    )
}
