package imdb

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object IMDBClient {
  private def quotesUrl(movieId: String) = s"https://www.imdb.com/title/${movieId}/quotes/"

  def getMovePage(movieId: String)(implicit as: ActorSystem, ex: ExecutionContext): Future[String] =
    Http()
      .singleRequest(HttpRequest(uri = quotesUrl(movieId)))
      .flatMap(checkStatus(_))
      .flatMap(_.entity.toStrict(10.second))
      .map(_.getData().utf8String)

  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response                                      => Future.failed(new Exception(s"IMDB responded with status code ${response.status}"))
    }

}
