package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.Pages
import frontend.pages._
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.web.PathAndQuery._
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]
}

class Frontend(pages: Pages)(implicit as: ActorSystem, ex: ExecutionContext) {

  def route: Route = {

    object PageQP extends OQP("page")

    def router = Router[Future, FrontendState](
      toState = {
        case Root                                             => pages.game.indexPage.load
        case Root / "game"                                    => pages.game.questionPage.load
        case Root / "admin"                                   => pages.admin.indexPage.load
        case Root / "admin" / "agents"                        => pages.admin.agentsPage.load
        case Root / "admin" / "movies"                        => pages.admin.moviesPage.load
        case Root / "admin" / "movies" / movieId              => pages.admin.moviePage.load(movieId)
        case Root / "admin" / "translations" :?* PageQP(page) => pages.admin.translationsPage.load(page)
        case _                                                => _ => Future.successful(NotFoundState)
      },
      fromState = {
        case _: GameIndexState               => Root
        case _: AdminState                   => Root / "admin"
        case AdminAgentsState                => Root / "admin" / "agents"
        case _: AdminMoviesState             => Root / "admin" / "movies"
        case AdminMovieState(row)            => Root / "admin" / "movies" / row.movieId
        case AdminTranslationsState(page, _) => Root / "admin" / "translations" :? "page" -> page.toString
      }
    )

    def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
      case state: GameIndexState         => pages.game.indexPage.render(state)
      case state: GameQuestionState      => pages.game.questionPage.render(state)
      case state: AdminState             => pages.admin.indexPage.render(state)
      case state: AdminAgentsState.type  => pages.admin.agentsPage.render(state)
      case state: AdminTranslationsState => pages.admin.translationsPage.render(state)
      case state: AdminMoviesState       => pages.admin.moviesPage.render(state)
      case state: AdminMovieState        => pages.admin.moviePage.render(state)
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
