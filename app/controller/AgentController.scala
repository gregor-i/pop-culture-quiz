package controller

import agent.{
  Agent,
  GoogleTranslationAgent,
  IMDBMoviePageAgent,
  IMDBQuotesAgent,
  SpeechAgent,
  SystranTranslationAgent,
  TranslationAgent
}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.InjectedController

import javax.inject.{Inject, Singleton}

@Singleton()
class AgentController @Inject() (
    imdbMoviePageAgent: IMDBMoviePageAgent,
    imdbQuotesAgent: IMDBQuotesAgent,
    googleTranslationAgent: GoogleTranslationAgent,
    systranTranslationAgent: SystranTranslationAgent,
    speechAgent: SpeechAgent
) extends InjectedController {

  val agents: Map[String, Agent] = Map(
    "IMDBMoviePageAgent"      -> imdbMoviePageAgent,
    "IMDBQuotesAgent"         -> imdbQuotesAgent,
    "GoogleTranslationAgent"  -> googleTranslationAgent,
    "SystranTranslationAgent" -> systranTranslationAgent,
    "SpeechAgent"             -> speechAgent
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
        agent.start()
        Ok(s"Started ${agentName}")
      case None => NotFound
    }
  }

  def stop(agentName: String) = Action {
    agents.get(agentName) match {
      case Some(agent) =>
        agent.stop()
        Ok(s"Stopped ${agentName}")
      case None => NotFound
    }
  }

  def ui() = Action {
    Ok(views.html.admin.Agents(agents))
  }

}
