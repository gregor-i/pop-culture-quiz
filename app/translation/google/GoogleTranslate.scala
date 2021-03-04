package translation.google

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import io.circe.{Decoder, Json, parser}
import io.lemonlabs.uri.Url
import play.api.Logger
import translation.TranslationService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object GoogleTranslate extends TranslationService {
  val name = "Google"

  val defaultChain = Seq("ar", "bn", "zh-tw", "cs", "nl", "eo", "fi", "el", "ht", "iw", "ta", "uz", "vi", "cy", "xh", "yo")

  val logger = Logger(this.getClass)

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

  def apply(texts: Seq[String], src: String, dest: String)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] = {
    if (texts.isEmpty)
      Future.failed(new Exception("no texts given to translate"))
    else {
      logger.info(s"Translating ${texts.mkString(",").take(10)} from ${src} to ${dest}")
      for {
        response <- Http()
          .singleRequest(HttpRequest(uri = uri(text = texts, src = src, dest = dest)))
          .flatMap(checkStatus)
        body <- response.entity.toStrict(10.second)
        data    = body.getData().utf8String
        json    = parser.parse(data)
        decoded = json.flatMap(_.as(decoder))
        result <- decoded match {
          case Left(_) =>
            logger.warn("Decoding failure")
            Future.failed(new Exception(s"data ${data} could not be decoded"))
          case Right(translation) => Future.successful(translation)
        }
        _ <- Future {
          Thread.sleep(30000)
          true
        }
      } yield handleMultiSentenceTexts(result, texts)
    }
  }

  def decoder: Decoder[Map[String, String]] = Decoder.instance { cursor =>
    cursor.downArray
      .as(Decoder.decodeSeq(decoderTranslation))
      .map(_.flatten.toMap)
  }

  def handleMultiSentenceTexts(translations: Map[String, String], texts: Seq[String]): Map[String, String] = {
    texts.map { text =>
      val translated = translations.foldLeft(text) { (text, translation) =>
        text.replace(translation._1, translation._2)
      }

      text -> translated
    }.toMap
  }

  private def decoderTranslation: Decoder[Option[(String, String)]] = Decoder.instance { cursor =>
    Right {
      for {
        translation <- cursor.downN(0).as[String].toOption
        original    <- cursor.downN(1).as[String].toOption
      } yield (original.trim, translation.trim)
    }
  }

  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext, mat: Materializer): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response =>
        logger.warn(s"Google translate did not respond with Ok, but with ${response.status}")
        response.discardEntityBytes()
        Future.failed(new Exception(s"Google Translate responded with status code ${response.status}"))
    }
}
