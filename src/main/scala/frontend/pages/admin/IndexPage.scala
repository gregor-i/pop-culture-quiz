package frontend.pages.admin

import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{AdminState, FrontendState, Page}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._
import repo.TranslationRepo

import scala.concurrent.{ExecutionContext, Future}

class IndexPage(translationRepo: TranslationRepo)(implicit ex: ExecutionContext) extends Page[AdminState] {

  def fromState: PartialFunction[FrontendState, PathAndQuery] = { case _: AdminState =>
    Root / "admin"
  }

  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = { case Root / "admin" =>
    state =>
      Future {
        AdminState(
          state.deviceId,
          progress = translationRepo.progress()
        )
      }
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
              seqToNode(state.progress.map { case (key, count) =>
                li(b(key, ": "), count.toString)
              })
            )
          )
        )
      )
    }
}
