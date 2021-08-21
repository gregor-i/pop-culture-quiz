package frontend.pages.admin

import di.Global
import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{AdminState, FrontendState, Page}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.{ExecutionContext, Future}

object IndexPage extends Page[AdminState] {
  def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[AdminState] =
    Future {
      AdminState(
        progress = global.repo.translationRepo.progress()
      )
    }

  def render(state: AdminState): Node =
    optimize {
      Html(
        Common.head("Admin"),
        body(
          div(
            `class` := "block",
            h1(`class` := "title", s"Pop-Culture-Quiz Admin"),
            div(a(href := "/admin/movies", "Movies")),
            div(a(href := "/admin/agents", "Agents")),
            div(a(href := "/admin/translations", "Translations"))
          ),
          div(
            `class` := "block",
            h2(`class` := "title", "Translation Progress"),
            ul(
              seqToNode(state.progress.map {
                case (key, count) =>
                  li(b(key, ": "), count.toString)
              })
            )
          )
        )
      )
    }
}
