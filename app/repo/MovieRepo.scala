package repo

import anorm._
import io.circe.syntax._
import model.{Quote, QuoteCrawlerState}
import play.api.db.Database

import javax.inject.{Inject, Singleton}

case class MovieRow(movieId: String, state: QuoteCrawlerState)

@Singleton
class MovieRepo @Inject() (db: Database) {
  private def parser: RowParser[MovieRow] =
    for {
      movieId <- SqlParser.str("movie_id")
      state <-SqlParser.str("state").collect("state could not be decoded") {
        case StateData(quote) => quote
      }.?
    } yield MovieRow(movieId, state.getOrElse(QuoteCrawlerState.NotCrawled))

  object StateData {
    def unapply(data: String): Option[QuoteCrawlerState] =
      io.circe.parser.decode(data)(QuoteCrawlerState.codec).toOption
  }

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
            SET state = ${state.asJson.noSpaces}
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
