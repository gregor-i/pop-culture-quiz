package agent

import akka.stream.UniqueKillSwitch
import play.api.Logger

trait Agent {
  private val logger                               = Logger(classOf[Agent])
  private var killSwitch: Option[UniqueKillSwitch] = None

  def start(): Unit =
    killSwitch match {
      case Some(_) =>
        logger.info(s"Tried to start ${this.getClass.getSimpleName}, but was already running.")
      case None =>
        killSwitch = Some(startStream())
        logger.info(s"Started ${this.getClass.getSimpleName}.")
    }

  def stop(): Unit = killSwitch match {
    case Some(switch) =>
      switch.shutdown()
      killSwitch = None
      logger.info(s"Stopped ${this.getClass.getSimpleName}.")
    case None =>
      logger.info(s"Tried to stop ${this.getClass.getSimpleName}, but was not running.")
  }

  def running: Boolean = killSwitch.isDefined

  private[agent] def startStream(): UniqueKillSwitch
}
