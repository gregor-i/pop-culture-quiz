package repo

import play.api.db.Database

import javax.inject.{Inject, Singleton}
import anorm._

import java.time.{Instant, ZoneOffset, ZonedDateTime}

case class MovieRow(movieId: String, title: String, lastCrawledAt: Option[ZonedDateTime])

@Singleton
class MovieRepo @Inject() (db: Database) {
  private def parser: RowParser[MovieRow] =
    for {
      movieId       <- SqlParser.str("movie_id")
      title         <- SqlParser.str("title")
      lastCrawledAt <- SqlParser.get[Instant]("last_crawled_at").map(ZonedDateTime.ofInstant(_, ZoneOffset.UTC)).?
    } yield MovieRow(movieId, title, lastCrawledAt)

  def list(): Seq[MovieRow] =
    db.withConnection { implicit con =>
      SQL"""SELECT * FROM movies"""
        .as(parser.*)
    }

  def get(movieId: String): Option[MovieRow] =
    db.withConnection{      implicit con =>
      SQL"""SELECT * FROM movies WHERE movie_id = ${movieId}"""
        .as(parser.singleOpt)
    }

  def listNotCrawled(): Seq[MovieRow] =
    db.withConnection{ implicit  con =>
      SQL"""SELECT * FROM movies WHERE last_crawled_at IS NULL"""
        .as(parser.*)
    }

  def addNewMovie(movieId: String, title: String): Int =
    db.withConnection { implicit con =>
      SQL"""INSERT INTO movies (movie_id, title)
            VALUES ($movieId, $title)
      """
        .executeUpdate()
    }

  def markMovieAsCrawled(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""UPDATE movies
            SET last_crawled_at = ${ZonedDateTime.now()}
            WHERE movie_id = ${movieId}
         """
        .executeUpdate()
    }

  def delete(movieId: String): Int =
    db.withConnection { implicit con =>
      SQL"""DELETE FROM movies WHERE movie_id = ${movieId}"""
        .executeUpdate()
    }
}
