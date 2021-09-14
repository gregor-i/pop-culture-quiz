package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.{Agents, Repo}
import frontend.Frontend.globalContext.Node
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server.*
import korolev.state.javaSerialization.*
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}
import scala.deriving.Mirror

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]
}

class Frontend(agents: Agents, repo: Repo)(implicit as: ActorSystem, ex: ExecutionContext) {
  private object adminPages {
    import pages.admin._
    val indexPage        = new IndexPage(repo.translationRepo)
    val agentsPage       = new AgentsPage(agents)
    val moviePage        = new MoviePage(repo.movieRepo)
    val moviesPage       = new MoviesPage(repo.movieRepo)
    val translationsPage = new TranslationsPage(repo.translationRepo)

    val all = Seq(indexPage, agentsPage, moviePage, moviesPage, translationsPage)
  }

  private object gamePages {
    import pages.game._
    val questionPage = new QuestionPage(repo.questionService)
    val indexPage    = new IndexPage(questionPage)
    val roomPage     = new RoomPage()

    val all = Seq(questionPage, indexPage, roomPage)
  }

  private object staticPages {
    val notFoundPage = frontend.pages.NotFoundPage
    val loadingPage  = frontend.pages.LoadingPage

    val all = Seq(notFoundPage, loadingPage)
  }

  private val allPages: Seq[Page[_]] = adminPages.all ++ gamePages.all ++ staticPages.all

  private def router = Router[Future, FrontendState](
    toState = allPages
      .map(_.toState)
      .reduce(_ orElse _)
      .orElse({ case _ =>
        state => Future.successful(NotFoundState(state.deviceId))
      }),
    fromState = allPages.map(_.fromState).reduce(_ orElse _)
  )

  private def render(state: FrontendState): Node = state match {
    case s: frontend.GameIndexState         => gamePages.indexPage.render(s)
    case s: frontend.RoomState              => gamePages.roomPage.render(s)
    case s: frontend.GameQuestionState      => gamePages.questionPage.render(s)
    case s: frontend.AdminState             => adminPages.indexPage.render(s)
    case s: frontend.AdminAgentsState       => adminPages.agentsPage.render(s)
    case s: frontend.AdminMoviesState       => adminPages.moviesPage.render(s)
    case s: frontend.AdminMovieState        => adminPages.moviePage.render(s)
    case s: frontend.AdminTranslationsState => adminPages.translationsPage.render(s)
    case s: frontend.NotFoundState          => staticPages.notFoundPage.render(s)
    case s: frontend.LoadingState           => staticPages.loadingPage.render(s)
  }

  def route: Route = {

    val config = KorolevServiceConfig[Future, FrontendState, Any](
      stateLoader = StateLoader.forDeviceId(deviceId => Future.successful(NotFoundState(deviceId))),
      document = render,
      router = router
    )

    akkaHttpService(config = config).apply(AkkaHttpServerConfig())
  }
}
