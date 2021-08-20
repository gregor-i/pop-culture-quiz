import agent.Agent
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import di.Agents
import korolev.{Context, Router}
import korolev.akka.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server._
import korolev.state.javaSerialization._
import korolev.web.PathAndQuery._
import play.api.routing.Router.Routes
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

class Frontend(agents: Agents)(implicit as: ActorSystem, ex: ExecutionContext) {

  private val globalContext = Context[Future, FrontendState, Any]

  import globalContext._
  import levsha.dsl._
  import html._

  private def router = Router[Future, FrontendState](
    toState = {
      case Root / "admin" / "agents" => _ => Future.successful(AdminAgentsState(0))
      case _                         => _ => Future.successful(NotFoundState)
    },
    fromState = {
      case AdminAgentsState(_) => Root / "admin" / "agents"
    }
  )

  def head(title: String): levsha.Document.Node[Context.Binding[Future, Nothing, Any]] = html.head(
    html.title(title),
    // todo: host bulma and font awesome myself.
    link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/bulma@0.9.1/css/bulma.min.css"),
    link(
      rel := "stylesheet",
      href := "https://pro.fontawesome.com/releases/v5.10.0/css/all.css",
      integrity := "sha384-AYmEC3Yw5cVb3ZcuHtOA93w35dYTsvhLPVnYs9eStHfGJvOvKxVfELGroGkvsg+p",
      crossorigin := "anonymous"
    ),
    meta(name := "viewport", content := "width=device-width, initial-scale=1")
  )

  private def render: FrontendState => levsha.Document.Node[Context.Binding[Future, FrontendState, Any]] = {
    case AdminAgentsState(i) =>
      optimize {
        Html(
          head("Admin / Agents"),
          body(
            h1(`class` := "title", s"Pop-Culture-Quiz Admin: Agents ${i}"),
            table(
              `class` := "table",
              thead(
                tr(
                  td("Agent"),
                  td("Running"),
                  td()
                )
              ),
              tbody(
                seqToNode(agents.all.map(renderAgent))
              )
            )
          )
        )
      }
    case NotFoundState =>
      optimize {
        Html(
          head("404: Not Found"),
          body(
            div(
              `class` := "section",
              article(
                `class` := "message is-danger",
                div(
                  `class` := "message-body",
                  div(`class` := "title", "404: NotFound."),
                  a(href := "/", "return to landing page")
                )
              )
            )
          )
        )
      }
    case LoadingState =>
      optimize {
        Html(
          head("Loading ... "),
          body(
            i(
              `class` := "fa fa-spinner fa-pulse has-text-primary",
              position @= "absolute",
              left @= "50%",
              top @= "50%",
              marginLeft @= "-5rem",
              fontSize @= "10rem"
            )
          )
        )
      }
  }

  private val config = KorolevServiceConfig[Future, FrontendState, Any](
    stateLoader = StateLoader.default(LoadingState),
    document = render,
    router = router
  )

  private def startAgent(agent: Agent): Access => Future[Unit] =
    _.transition { state =>
      println(state)
      println(s"start ${agent.name}")
      agent.start()
      state
    }

  private def stopAgent(agent: Agent): Access => Future[Unit] =
    _.transition { state =>
      println(state)
      println(s"stop ${agent.name}")
      agent.stop()
      state
    }

  private def renderAgent(agent: Agent): Node =
    tr(
      td(agent.name),
      td(if (agent.running) "running" else "stopped"),
      td(
        if (agent.running)
          button(`class` := "button", "Stop", event("click")(stopAgent(agent)))
        else
          button(`class` := "button", "Start", event("click")(startAgent(agent)))
      )
    )

  val route: Route = akkaHttpService(config = config).apply(AkkaHttpServerConfig())
}
