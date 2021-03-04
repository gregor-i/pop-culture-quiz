package controller

import akka.actor.ActorSystem
import play.api.libs.json.JsObject
import play.api.mvc.InjectedController
import repo.{MovieRepo, MovieRow, TranslationRepo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
@Singleton
class DataController @Inject() (movieRepo: MovieRepo, translationRepo: TranslationRepo)(
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

  def enqueueTranslation(movieId: String, quoteId: String) = Action(parse.json) { request =>
    val parsed: Option[(String, Seq[String])] = for {
      body    <- request.body.asOpt[JsObject]
      service <- body.value.get("service").flatMap(_.asOpt[String])
      chain   <- body.value.get("chain").flatMap(_.asOpt[Seq[String]])
    } yield (service, chain)

    (for {
      parsedBody <- parsed.toRight(BadRequest("body could not be parsed"))
      movie      <- movieRepo.get(movieId).toRight(NotFound("Movie not found"))
      quote      <- movie.quotes.toOption.flatMap(_.get(quoteId)).toRight(NotFound("Quote not found"))
    } yield {
      translationRepo.enqueue(
        movieId = movieId,
        quoteId = quoteId,
        quote = quote,
        translationService = parsedBody._1,
        translationChain = parsedBody._2
      )
      Accepted
    }).merge
  }

  def enqueueTopQuotes(count: Int) = Action(parse.json) { request =>
    val parsed: Option[(String, Seq[String])] = for {
      body    <- request.body.asOpt[JsObject]
      service <- body.value.get("service").flatMap(_.asOpt[String])
      chain   <- body.value.get("chain").flatMap(_.asOpt[Seq[String]])
    } yield (service, chain)

    (for {
      parsedBody <- parsed.toRight(BadRequest("body could not be parsed"))
    } yield {
      movieRepo
        .list()
        .flatMap {
          case MovieRow(movieId, _, Right(quotes)) => quotes.map { case (quoteId, quote) => (movieId, quoteId, quote) }
          case _                                   => Seq.empty
        }
        .sortBy(_._3.score)
        .reverse
        .take(count)
        .foreach {
          case (movieId, quoteId, quote) =>
            translationRepo.enqueue(
              movieId = movieId,
              quoteId = quoteId,
              quote = quote,
              translationService = parsedBody._1,
              translationChain = parsedBody._2
            )
        }
      Accepted
    }).merge
  }
}
