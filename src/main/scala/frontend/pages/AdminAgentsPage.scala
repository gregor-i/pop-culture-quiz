package frontend.pages

import dataprocessing.agent.Agent
import di.Global
import frontend.Frontend.globalContext._
import frontend.{AdminAgentsState, FrontendState, Page}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.{ExecutionContext, Future}

object AdminAgentsPage extends Page[AdminAgentsState] {
  def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[AdminAgentsState] =
    Future.successful(AdminAgentsState(global.agents))

  def render(state: AdminAgentsState): Node =
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
              seqToNode(state.agents.all.map(renderAgent))
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
