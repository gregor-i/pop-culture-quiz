package imdb

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object IMDBClient {
  private def movieUrl(movieId: String)  = s"https://www.imdb.com/title/${movieId}/"
  private def quotesUrl(movieId: String) = s"https://www.imdb.com/title/${movieId}/quotes/"

  private val logger = LoggerFactory.getLogger(this.getClass)

  def getMoviePage(movieId: String)(implicit as: ActorSystem, ex: ExecutionContext): Future[String] = {
    logger.info(s"Loading movie page of ${movieId}")
    Http()
      .singleRequest(HttpRequest(uri = movieUrl(movieId)))
      .flatMap(checkStatus(_))
      .flatMap(_.entity.toStrict(10.second))
      .map(_.getData().utf8String)
  }

  def getQuotesPage(movieId: String)(implicit as: ActorSystem, ex: ExecutionContext): Future[String] = {
    logger.info(s"Loading quotes page of ${movieId}")
    Http()
      .singleRequest(HttpRequest(uri = quotesUrl(movieId)))
      .flatMap(checkStatus(_))
      .flatMap(_.entity.toStrict(10.second))
      .map(_.getData().utf8String)
  }

  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response =>
        logger.warn(s"IMDB did not respond with Ok, but with ${response.status}")
        Future.failed(new Exception(s"IMDB responded with status code ${response.status}"))
    }

}
