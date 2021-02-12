package controller

import akka.actor.ActorSystem
import imdb.{IMDBClient, IMDBParser}
import play.api.libs.json.JsObject
import play.api.mvc.InjectedController
import repo.{MovieRepo, QuoteRepo, TranslationRepo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DataController @Inject() (movieRepo: MovieRepo, quoteRepo: QuoteRepo, translationRepo: TranslationRepo)(
    implicit as: ActorSystem,
    ex: ExecutionContext
) extends InjectedController {
  def registerMovie(movieId: String) = Action {
    movieRepo.get(movieId) match {
      case Some(_) =>
        Accepted("Movie already registered")
      case None =>
        movieRepo.addNewMovie(movieId)
        Accepted("Movie registered and scheduled for crawling")
    }
  }

  def enqueueTranslation(quoteId: String) = Action(parse.json) { request =>
    val parsed: Option[(String, Seq[String])] = for {
      body    <- request.body.asOpt[JsObject]
      service <- body.value.get("service").flatMap(_.asOpt[String])
      chain   <- body.value.get("chain").flatMap(_.asOpt[Seq[String]])
    } yield (service, chain)

    (for {
      parsedBody <- parsed.toRight(BadRequest("body could not be parsed"))
      quote      <- quoteRepo.find(quoteId).toRight(NotFound("Quote not found"))
    } yield {
      translationRepo.enqueue(
        quoteId = quote.quoteId,
        translationService = parsedBody._1,
        translationChain = parsedBody._2
      )
      Accepted("")
    }).merge
  }
}
