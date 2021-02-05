package googleTranslate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import io.circe.{Decoder, Json, parser}
import io.lemonlabs.uri.Url

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object GoogleTranslate {
  def uri(text: Seq[String], src: String, dest: String) =
    Url(scheme = "https", host = "translate.googleapis.com", path = "/translate_a/single")
      .addParam("client", "gtx")
      .addParam("sl", src)
      .addParam("tl", dest)
      .addParam("hl", "en")
      .addParam("dt", "at")
      .addParam("dt", "bd")
      .addParam("dt", "ex")
      .addParam("dt", "ld")
      .addParam("dt", "md")
      .addParam("dt", "qca")
      .addParam("dt", "rw")
      .addParam("dt", "rm")
      .addParam("dt", "ss")
      .addParam("dt", "t")
      .addParam("ie", "UTF-8")
      .addParam("oe", "UTF-8")
      .addParam("otf", "1")
      .addParam("ssel", "0")
      .addParam("tsel", "0")
      .addParam("tk", "xxxx")
      .addParam("q", text.mkString("\n"))
      .toStringPunycode

  def apply(text: Seq[String], src: String, dest: String)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] =
    for {
      response <- Http()
        .singleRequest(HttpRequest(uri = uri(text = text, src = src, dest = dest)))
        .flatMap(checkStatus)
      body <- response.entity.toStrict(10.second)
      data    = body.getData().utf8String
      json    = parser.parse(data)
      decoded = json.flatMap(_.as(decoder))
      result <- decoded match {
        case Left(_)            => Future.failed(new Exception(s"data ${data} could not be decoded"))
        case Right(translation) => Future.successful(translation)
      }
    } yield result

  def decoder: Decoder[Map[String, String]] = Decoder.instance { cursor =>
    cursor.downArray
      .as(Decoder.decodeSeq(decoderTranslation))
      .map(_.flatten.toMap)
  }

  private def decoderTranslation: Decoder[Option[(String, String)]] = Decoder.instance { cursor =>
    Right {
      for {
        translation <- cursor.downN(0).as[String].toOption
        original    <- cursor.downN(1).as[String].toOption
      } yield (original.trim, translation.trim)
    }
  }

  // 429 means bann
  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response                                      => Future.failed(new Exception(s"Google Translate responded with status code ${response.status}"))
    }
}
