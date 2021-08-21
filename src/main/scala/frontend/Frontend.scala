package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.Global
import frontend.pages._
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.web.PathAndQuery._
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]

  def apply(global: Global)(implicit as: ActorSystem, ex: ExecutionContext): Route = {

    object PageQP extends OQP("page")

    def router = Router[Future, FrontendState](
      toState = {
        case Root / "admin"                                   => AdminPage.load(global)
        case Root / "admin" / "agents"                        => AdminAgentsPage.load(global)
        case Root / "admin" / "movies"                        => AdminMoviesPage.load(global)
        case Root / "admin" / "movies" / movieId              => AdminMoviePage.load(global, movieId)
        case Root / "admin" / "translations" :?* PageQP(page) => AdminTranslationsPage.load(global, page)
        case _                                                => _ => Future.successful(NotFoundState)
      },
      fromState = {
        case _: AdminState                   => Root / "admin"
        case _: AdminAgentsState             => Root / "admin" / "agents"
        case _: AdminMoviesState             => Root / "admin" / "movies"
        case AdminMovieState(row)            => Root / "admin" / "movies" / row.movieId
        case AdminTranslationsState(page, _) => Root / "admin" / "translations" :? "page" -> page.toString
      }
    )

    def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
      case state: AdminState             => AdminPage.render(state)
      case state: AdminAgentsState       => AdminAgentsPage.render(state)
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
