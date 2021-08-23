package frontend.pages.admin

import dataprocessing.agent.Agent
import di.Agents
import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{AdminAgentsState, FrontendState, Page}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.{ExecutionContext, Future}

class AgentsPage(agents: Agents)(implicit ex: ExecutionContext) extends Page[AdminAgentsState.type] {

  def fromState: PartialFunction[FrontendState, PathAndQuery] = {
    case AdminAgentsState => Root / "admin" / "agents"
  }

  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = {
    case Root / "admin" / "agents" => _ => Future.successful(AdminAgentsState)
  }

  def render(state: AdminAgentsState.type): Node =
    optimize {
      Html(
        Common.head("Admin / Agents"),
        body(
          h1(`class` := "title", s"Pop-Culture-Quiz Admin: Agents"),
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

  private def startAgent(agent: Agent): Access => Future[Unit] =
    _.transition { state =>
      agent.start()
      state
    }

  private def stopAgent(agent: Agent): Access => Future[Unit] =
    _.transition { state =>
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
}
