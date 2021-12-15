package dataprocessing.translation.systran

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import io.circe.{Decoder, parser}
import io.lemonlabs.uri.Url
import org.slf4j.LoggerFactory
import dataprocessing.translation.TranslationService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object SystranTranslate extends TranslationService {
  val apiKey = "4bd8ecae-24e7-4df9-8747-3230ce9abe6c"

  val name = "Systran"

  private val logger = LoggerFactory.getLogger(this.getClass)

  val supportedLanguages = Set(
    "ar", // Arabic,
    "zh", // Chinese,
    "nl", // Dutch,
    "en", // English,
    "fr", // French,
    "de", // German,
    "el", // Greek,
    "it", // Italian,
    "ja", // Japanese,
    "ko", // Korean,
    "pl", // Polish,
    "pt", // Portuguese,
    "ru", // Russian,
    "es", // Spanish,
    "sv"  // Swedish
  )

  val defaultChain = Seq(
    "ar", // Arabic,
    "en",
    "nl", // Dutch,
    "en", // English,
    "zh", // Chinese,
    "fr", // French,
    "en",
    "sv" // Swedish
  )

  def uri(text: Seq[String], src: String, dest: String) =
    text
      .foldLeft(Url(scheme = "https", host = "api-platform.systran.net", path = "/translation/text/translate")) {
        _.addParam("input", _)
      }
      .addParam("source", src)
      .addParam("target", dest)
      .addParam("key", apiKey)
      .toStringPunycode

  def apply(texts: Seq[String], src: String, dest: String)(implicit
      as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] = {
    require(texts.nonEmpty, "no texts given to translate")
    require(supportedLanguages.contains(src), "source language not supported")
    require(supportedLanguages.contains(dest), "destination language not supported")
    for {
      response <- Http()
        .singleRequest(HttpRequest(uri = uri(text = texts, src = src, dest = dest)))
        .flatMap(checkStatus)
      body <- response.entity.toStrict(10.second)
      data    = body.getData().utf8String
      json    = parser.parse(data)
      decoded = json.flatMap(_.as(decoder))
      result <- decoded match {
        case Left(_)            => Future.failed(new Exception(s"data ${data} could not be decoded"))
        case Right(translation) => Future.successful(translation)
      }
//      _ = println(texts.zip(result).toMap)
    } yield texts.zip(result).toMap
  }

  private def checkStatus(response: HttpResponse)(implicit ex: ExecutionContext, mat: Materializer): Future[HttpResponse] =
    response match {
      case response if response.status == StatusCodes.OK => Future.successful(response)
      case response =>
        response.discardEntityBytes()
        logger.warn(s"Systran translate did not respond with Ok, but with ${response.status}")

        Future.failed(new Exception(s"Systran responded with status code ${response.status}"))
    }

  def decoder: Decoder[Seq[String]] = Decoder.instance { cursor =>
    val outputDecoder: Decoder[String] = Decoder.instance(_.downField("output").as[String])

    cursor.downField("outputs").as(Decoder.decodeSeq(outputDecoder))
  }
}
