package googleTranslate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}

import io.circe.parser

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object GoogleTranslate {
  private implicit val as: ActorSystem = ActorSystem("google-translate")
  private implicit val dispatcher:ExecutionContext = as.getDispatcher

  def uri(text: String, src: String, dest: String) = s"https://translate.googleapis.com/translate_a/single?client=gtx&sl=${src}&tl=${dest}&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&tk=xxxx&q=${text.trim.replace(" ", "+")}"

  def apply(text: String, src: String, dest: String): Future[String] =
    Http().singleRequest(HttpRequest(uri = uri(text=text, src=src, dest=dest)))
      .flatMap(checkStatus)
      .flatMap(checkStatus(_))
      .flatMap(_.entity.toStrict(10.second))
      .map(_.getData().utf8String)
      .flatMap( data => parse(data).fold(Future.failed, Future.successful) )

  private def parse(data: String) =
    parser.parse(data)
      .flatMap(_.hcursor.downN(0).downN(0).downN(0).as[String])


  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response => Future.failed(new Exception(s"Google Translate responded with status code ${response.status}"))
    }
}
