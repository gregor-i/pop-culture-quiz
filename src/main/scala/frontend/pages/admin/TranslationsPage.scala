package frontend.pages.admin

import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{AdminTranslationsState, FrontendState}
import levsha.dsl._
import levsha.dsl.html._
import model.TranslationState
import repo.{TranslationRepo, TranslationRow}

import scala.concurrent.{ExecutionContext, Future}

class TranslationsPage(translationRepo: TranslationRepo) /*extends Page[AdminTranslationsState]*/ {

  def load(pageQP: Option[String])(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState] =
    Future {
      val pageNumber   = pageQP.flatMap(_.toIntOption).getOrElse(1)
      val translations = translationRepo.list(offset = (pageNumber - 1) * 100)
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
