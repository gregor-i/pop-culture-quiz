import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import di.{Agents, Pages, Repo}
import frontend.{Assets, Frontend}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object Main {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName.stripSuffix("$"))

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                = ActorSystem()
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val materializer: Materializer         = Materializer(system)

    val config = ConfigFactory.load()

    val repo   = new Repo(config)
    val agents = new Agents(repo)
    val pages  = new Pages(agents, repo)

    repo.db.withConnection(_ => logger.info("Database connection established."))

    logger.info("applying schema evolutions.")
    repo.setupSchema()
    logger.info("finished applying evolutions.")

    agents.all.filter(_.autostart).foreach { agent =>
      logger.info(s"auto starting ${agent.name}")
      agent.start()
    }

    val frontend = new Frontend(pages)

    val port = config.getInt("http.port")
    Http().newServerAt("0.0.0.0", port).bind(Assets.routes ~ frontend.route)
    logger.info(s"Server now online on port ${port}.")
  }
}
