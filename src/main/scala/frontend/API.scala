package frontend

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaType, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import model.SpeechState.Processed
import org.apache.commons.net.util.Base64
import repo.TranslationRepo

class API(translationRepo: TranslationRepo) {
  val routes: Route = speech

  def speech: Route = (path("api" / "translations" / IntNumber / "speech") & get) { (id: Int) =>
    translationRepo.getTranslation(id).map(_.speech) match {
      case Some(Processed(url)) =>
        val rawBytes  = Base64.decodeBase64(url)
        val mediaType = MediaType.audio("mpeg", MediaType.Compressible)
        complete(HttpEntity(mediaType, rawBytes))
      case Some(other) => complete(StatusCodes.BadRequest -> other.toString)
      case None        => complete(StatusCodes.NotFound   -> "translation not found")
    }
  }
}
