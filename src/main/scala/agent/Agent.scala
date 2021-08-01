package agent

import akka.stream.UniqueKillSwitch
import org.slf4j.LoggerFactory

trait Agent {
  private val logger = LoggerFactory.getLogger(classOf[Agent])

  private var killSwitch: Option[UniqueKillSwitch] = None

  val name = this.getClass.getSimpleName

  def start(): Unit =
    killSwitch match {
      case Some(_) =>
        logger.info(s"Tried to start ${name}, but was already running.")
      case None =>
        killSwitch = Some(startStream())
        logger.info(s"Started ${name}.")
    }

  def stop(): Unit = killSwitch match {
    case Some(switch) =>
      switch.shutdown()
      killSwitch = None
      logger.info(s"Stopped ${name}.")
    case None =>
      logger.info(s"Tried to stop ${name}, but was not running.")
  }

  def running: Boolean = killSwitch.isDefined

  private[agent] def startStream(): UniqueKillSwitch
}

trait Autostart { _: Agent =>
  this.start()
}
