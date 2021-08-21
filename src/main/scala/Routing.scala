import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshallable}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import di.{Agents, Repo}
import play.api.http.Status
import repo.MovieRow
import dataprocessing.service.HideCharacterNames

import scala.util.Random

class Routing(repo: Repo) { routing =>

  implicit private val twirl: Marshaller[play.twirl.api.Content, HttpResponse] =
    ToResponseMarshallable.marshaller
      .compose(content => HttpEntity.Strict(ContentTypes.`text/html(UTF-8)`, ByteString(content.body)))

  val routes = concat(GameRoutes.all, AdminRoutes.all)

  object GameRoutes {
    def all = concat(indexRoute, gameRoute)
    def indexRoute = pathEndOrSingleSlash {
      get { complete(game.html.Start()) }
    }

    def gameRoute = path("game") {
      parameters(
        "releaseYearMin".as[Int].withDefault(1900),
        "releaseYearMax".as[Int].withDefault(2100),
        "readOutQuote".as[Boolean].withDefault(false)
      ) { (releaseYearMin, releaseYearMax, readOutQuote) =>
        get {
          repo.questionService.getOne(
            releaseYearMax = releaseYearMax,
            releaseYearMin = releaseYearMin,
            readOutQuote = readOutQuote
          ) match {
            case Some(question) =>
              complete(
                game.html
                  .Game(
                    translation = HideCharacterNames(question.translatedQuote),
                    original = question.originalQuote,
                    correctMovie = question.correctMovie,
                    movies = Random.shuffle((question.correctMovie +: question.otherMovies)),
                    spokenQuoteDataUrl = question.spokenQuoteDataUrl
                  )
              )
            case None =>
              complete(HttpResponse(Status.NOT_FOUND))
          }
        }
      }
    }
  }

  object AdminRoutes {
    def all = pathPrefix("admin")(concat(translations, movieQuotes))

    def translations = (get & path("translations") & parameter("page".as[Int].withDefault(1))) { page =>
      val translations = repo.translationRepo.list(offset = 100 * page, limit = 100)
      val progress     = repo.translationRepo.progress()
      complete(admin.html.Translations(translations, progress))
    }

    def movieQuotes = (get & path("movies" / Segment)) { movieId =>
      repo.movieRepo.get(movieId) match {
        case Some(MovieRow(_, data, quotes)) =>
          complete(admin.html.MovieQuotes(movieId, data, quotes))
        case None =>
          complete(Status.NOT_FOUND, "movie not found")
      }
    }
  }
}
