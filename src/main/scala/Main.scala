import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import di.{Agents, Repo}
import org.slf4j.LoggerFactory
import play.api.db.evolutions.Evolutions

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object Main {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName.stripSuffix("$"))

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                = ActorSystem()
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val materializer: Materializer         = Materializer(system)

    val config = ConfigFactory.load()

    val repo    = new Repo(config)
    val agents  = new Agents(repo)
    val routing = new Routing(repo, agents)

    repo.db.withConnection(_ => logger.info("Database connection established."))

    logger.info("applying schema evolutions.")
    Evolutions.applyEvolutions(repo.db)
    logger.info("finished applying evolutions.")

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routing.routes)

    logger.info(s"Server now online.")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap { server =>
        logger.info("unbinding akka http server")
        server.unbind()
      }
      .flatMap { _ =>
        logger.info("terminating actor system")
        system.terminate()
      }
      .flatMap { _ =>
        logger.info("shutting down database connection")
        Future(repo.db.shutdown())
      }
      .onComplete(_ => logger.info("good bye"))
  }
}
