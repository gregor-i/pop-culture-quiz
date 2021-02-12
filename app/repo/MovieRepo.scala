package repo

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import anorm._
import io.circe.syntax._
import model.QuoteCrawlerState
import play.api.db.Database

import java.sql.Connection
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

case class MovieRow(movieId: String, state: QuoteCrawlerState)

@Singleton
class MovieRepo @Inject() (db: Database)(implicit mat: Materializer) extends JsonColumn {
  def parser: RowParser[MovieRow] =
    for {
      movieId <- SqlParser.str("movie_id")
      state   <- SqlParser.get[Either[io.circe.Error, QuoteCrawlerState]]("state").?
    } yield MovieRow(movieId, state.flatMap(_.toOption).getOrElse(QuoteCrawlerState.NotCrawled))

  def list(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies"""
        .as(parser.*)
    }

  def get(movieId: String): Option[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies WHERE movie_id = ${movieId}"""
        .as(parser.singleOpt)
    }

  def addNewMovie(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO movies (movie_id) VALUES ($movieId)"""
        .executeUpdate()
    }

  def setState(movieId: String, state: QuoteCrawlerState): Int =
    db.withConnection { implicit con =>
      SQL"""UPDATE movies
            SET state = ${state.asJson}
            WHERE movie_id = ${movieId}
         """
        .executeUpdate()
    }

  def truncate(): Int =
    db.withConnection { implicit con =>
      SQL"""TRUNCATE movies CASCADE"""
        .executeUpdate()
    }

  def listUnprocessed(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      val notCrawled: QuoteCrawlerState = QuoteCrawlerState.NotCrawled
      SQL"""SELECT * FROM movies WHERE state IS NULL OR state = ${notCrawled.asJson} LIMIT 10"""
        .as(parser.*)
    }
}
