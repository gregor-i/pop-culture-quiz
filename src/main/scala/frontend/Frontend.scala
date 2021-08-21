package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.{Agents, Repo}
import frontend.pages.{AdminAgentsPage, AdminMoviePage, AdminMoviesPage, LoadingPage, NotFoundPage}
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.web.PathAndQuery._
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]

  def apply(agents: Agents, repo: Repo)(implicit as: ActorSystem, ex: ExecutionContext): Route = {

    def router = Router[Future, FrontendState](
      toState = {
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
        case _ => _ => Future.successful(NotFoundState)
      },
      fromState = {
        case AdminAgentsState     => Root / "admin" / "agents"
        case _: AdminMoviesState  => Root / "admin" / "movies"
        case AdminMovieState(row) => Root / "admin" / "movies" / row.movieId
      }
    )

    def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
      case AdminAgentsState        => AdminAgentsPage.render(agents)
      case state: AdminMoviesState => AdminMoviesPage.render(state)
      case state: AdminMovieState  => AdminMoviePage.render(state)
      case NotFoundState           => NotFoundPage.render
      case LoadingState            => LoadingPage.render
    }

    val config = KorolevServiceConfig[Future, FrontendState, Any](
      stateLoader = StateLoader.default(LoadingState),
      document = render,
      router = router
    )

    akkaHttpService(config = config).apply(AkkaHttpServerConfig())
  }
}
