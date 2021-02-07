package controller

import agent.{Agent, IMDBAgent, TranslationAgent}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.InjectedController

import javax.inject.{Inject, Singleton}

@Singleton()
class AgentController @Inject() (IMDBAgent: IMDBAgent, translationAgent: TranslationAgent) extends InjectedController {

  val agents: Map[String, Agent] = Map(
    "IMDBAgent"        -> IMDBAgent,
    "translationAgent" -> translationAgent
  )

  def state() = Action {
    Ok(
      JsObject(
        agents.map {
          case (key, value) =>
            key -> JsString(if (value.running) "running" else "stopped")
        }.toSeq
      )
    )
  }

  def start(agentName: String) = Action {
    agents.get(agentName) match {
      case Some(agent) =>
        agent.running = true
        Ok(s"Started ${agentName}")
      case None => NotFound
    }
  }

  def stop(agentName: String) = Action {
    agents.get(agentName) match {
      case Some(agent) =>
        agent.running = false
        Ok(s"Stopped ${agentName}")
      case None => NotFound
    }
  }

  def ui() = Action {
    Ok(views.html.admin.Agents(agents))
  }

}
