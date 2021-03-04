package repo

import akka.stream.Materializer
import anorm._
import io.circe.Json
import io.circe.syntax._
import model.{MovieData, Quote, QuoteCrawlerState}
import play.api.db.Database

import javax.inject.{Inject, Singleton}

case class MovieRow(movieId: String, data: Either[String, MovieData], quotes: Either[String, Map[String, Quote]])

@Singleton
class MovieRepo @Inject() (db: Database)(implicit mat: Materializer) extends JsonColumn {
  def list(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies"""
        .as(MovieRepo.parser.*)
    }

  def get(movieId: String): Option[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE movie_id = ${movieId}"""
        .as(MovieRepo.parser.singleOpt)
    }

  def addNewMovie(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO movies (movie_id) VALUES ($movieId)"""
        .executeUpdate()
    }

  def setMovieData(movieId: String, movieData: Either[String, MovieData]): Int =
    db.withConnection { implicit con =>
      val data = movieData match {
        case Right(value) => value.asJson
        case Left(value)  => s"Error: ${value}".asJson
      }

      SQL"""UPDATE movies
            SET data = ${data.asJson}
            WHERE movie_id = ${movieId}
         """
        .executeUpdate()
    }

  def setQuotes(movieId: String, quotes: Either[String, Map[String, Quote]]): Int =
    db.withConnection { implicit con =>
      val data = quotes match {
        case Right(value) => value.asJson
        case Left(value)  => s"Error: ${value}".asJson
      }

      SQL"""UPDATE movies
            SET quotes = ${data}
            WHERE movie_id = ${movieId}
         """
        .executeUpdate()
    }

  def listNoData(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE data IS NULL LIMIT 10"""
        .as(MovieRepo.parser.*)
    }

  def listNoQuotes(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE quotes IS NULL LIMIT 10"""
        .as(MovieRepo.parser.*)
    }

  def truncate(): Int =
    db.withConnection { implicit con =>
      SQL"""TRUNCATE movies CASCADE"""
        .executeUpdate()
    }
}

object MovieRepo extends JsonColumn {
  def parser: RowParser[MovieRow] =
    for {
      movieId <- SqlParser.str("movie_id")
      data    <- SqlParser.get[Json]("data").?
      quotes  <- SqlParser.get[Json]("quotes").?
    } yield MovieRow(
      movieId = movieId,
      data = data.getOrElse(Json.Null).as[MovieData].left.map(_.getMessage()),
      quotes = quotes.getOrElse(Json.Null).as[Map[String, Quote]].left.map(_.getMessage())
    )
}
