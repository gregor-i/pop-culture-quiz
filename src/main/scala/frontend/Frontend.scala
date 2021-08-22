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
        case Root                                             => game.IndexPage.load(global)
        case Root / "game"                                    => game.QuestionPage.load(global)
        case Root / "admin"                                   => admin.IndexPage.load(global)
        case Root / "admin" / "agents"                        => admin.AgentsPage.load(global)
        case Root / "admin" / "movies"                        => admin.MoviesPage.load(global)
        case Root / "admin" / "movies" / movieId              => admin.MoviePage.load(global, movieId)
        case Root / "admin" / "translations" :?* PageQP(page) => admin.TranslationsPage.load(global, page)
        case _                                                => _ => Future.successful(NotFoundState)
      },
      fromState = {
        case _: GameIndexState               => Root
        case _: AdminState                   => Root / "admin"
        case _: AdminAgentsState             => Root / "admin" / "agents"
        case _: AdminMoviesState             => Root / "admin" / "movies"
        case AdminMovieState(row)            => Root / "admin" / "movies" / row.movieId
        case AdminTranslationsState(page, _) => Root / "admin" / "translations" :? "page" -> page.toString
      }
    )

    def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
      case state: GameIndexState         => game.IndexPage.render(global, state)
      case state: GameQuestionState      => game.QuestionPage.render(state, global)
      case state: AdminState             => admin.IndexPage.render(state)
      case state: AdminAgentsState       => admin.AgentsPage.render(state)
      case state: AdminTranslationsState => admin.TranslationsPage.render(state)
      case state: AdminMoviesState       => admin.MoviesPage.render(state)
      case state: AdminMovieState        => admin.MoviePage.render(state)
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
