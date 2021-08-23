package frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.Pages
import frontend.Frontend.globalContext.Node
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.{Context, Router}

import scala.concurrent.{ExecutionContext, Future}

object Frontend {
  val globalContext: Context[Future, FrontendState, Any] = Context[Future, FrontendState, Any]
}

class Frontend(pages: Pages)(implicit as: ActorSystem, ex: ExecutionContext) {

  def route: Route = {

    def router = Router[Future, FrontendState](
      toState = pages.all
        .map(_.toState)
        .reduce(_ orElse _)
        .orElse({
          case _ => _ => Future.successful(NotFoundState)
        }),
      fromState = pages.all.map(_.fromState).reduce(_ orElse _)
    )

    def render: FrontendState => Node =
      state =>
        pages.all
          .find(_.acceptState(state))
          .getOrElse(throw new Exception(s"No Page defined for '${state.getClass.getSimpleName}'"))
          .asInstanceOf[Page[FrontendState]]
          .render(state)

    val config = KorolevServiceConfig[Future, FrontendState, Any](
      stateLoader = StateLoader.default(LoadingState),
      document = render,
      router = router
    )

    akkaHttpService(config = config).apply(AkkaHttpServerConfig())
  }
}
