package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.{Agents, Repo}
import frontend.pages.{
  AdminAgentsPage,
  AdminMoviePage,
  AdminMoviesPage,
  AdminPage,
  AdminTranslationsPage,
  LoadingPage,
  NotFoundPage
}
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.web.PathAndQuery._
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]

  def apply(agents: Agents, repo: Repo)(implicit as: ActorSystem, ex: ExecutionContext): Route = {

    object PageQP extends OQP("page")

    def router = Router[Future, FrontendState](
      toState = {
        case Root / "admin" =>
          _ =>
            Future {
              AdminState(
                progress = repo.translationRepo.progress()
              )
            }
        case Root / "admin" / "agents" => _ => Future.successful(AdminAgentsState)
        case Root / "admin" / "movies" =>
          _ =>
            Future {
              val movies = repo.movieRepo.list().sortBy(_.movieId)
              AdminMoviesState(movies)
            }
        case Root / "admin" / "movies" / movieId =>
          _ =>
            Future {
              repo.movieRepo.get(movieId) match {
                case Some(row) => AdminMovieState(row)
                case None      => NotFoundState
              }
            }
        case Root / "admin" / "translations" :?* PageQP(page) =>
          _ =>
            Future {
              val pageNumber   = page.flatMap(_.toIntOption).getOrElse(1)
              val translations = repo.translationRepo.list(offset = (pageNumber - 1) * 100)
              AdminTranslationsState(pageNumber, translations)
            }
        case _ => _ => Future.successful(NotFoundState)
      },
      fromState = {
        case _: AdminState             => Root / "admin"
        case AdminAgentsState          => Root / "admin" / "agents"
        case _: AdminTranslationsState => Root / "admin" / "translations"
        case _: AdminMoviesState       => Root / "admin" / "movies"
        case AdminMovieState(row)      => Root / "admin" / "movies" / row.movieId
      }
    )

    def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
      case state: AdminState             => AdminPage.render(state)
      case AdminAgentsState              => AdminAgentsPage.render(agents)
      case state: AdminTranslationsState => AdminTranslationsPage.render(state)
      case state: AdminMoviesState       => AdminMoviesPage.render(state)
      case state: AdminMovieState        => AdminMoviePage.render(state)
      case NotFoundState                 => NotFoundPage.render
      case LoadingState                  => LoadingPage.render
    }

    val config = KorolevServiceConfig[Future, FrontendState, Any](
      stateLoader = StateLoader.default(LoadingState),
      document = render,
      router = router
    )

    akkaHttpService(config = config).apply(AkkaHttpServerConfig())
  }
}
