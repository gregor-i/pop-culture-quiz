import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import di.{Agents, Global, Repo}
import frontend.Frontend
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object Main {
  val logger = LoggerFactory.getLogger(this.getClass.getSimpleName.stripSuffix("$"))

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                = ActorSystem()
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val materializer: Materializer         = Materializer(system)

    val config = ConfigFactory.load()

    val repo    = new Repo(config)
    val agents  = new Agents(repo)
    val routing = new Routing(repo)
    val global  = new Global(agents, repo)

    repo.db.withConnection(_ => logger.info("Database connection established."))

    logger.info("applying schema evolutions.")
    repo.setupSchema()
    logger.info("finished applying evolutions.")

    agents.all.filter(_.autostart).foreach { agent =>
      logger.info(s"Autostarting ${agent.name}")
      agent.start()
    }

    val port = config.getInt("http.port")
    val bindingFuture =
      Http().newServerAt("0.0.0.0", port).bind(routing.routes ~ Frontend(global))

    logger.info(s"Server now online on port ${port}.")
//    StdIn.readLine() // let it run until user presses return
//    bindingFuture
//      .flatMap { server =>
//        logger.info("unbinding akka http server")
//        server.unbind()
//      }
//      .flatMap { _ =>
//        logger.info("terminating actor system")
//        system.terminate()
//      }
//      .flatMap { _ =>
//        logger.info("shutting down database connection")
//        Future(repo.db.shutdown())
//      }
//      .onComplete(_ => logger.info("good bye"))
  }
}
