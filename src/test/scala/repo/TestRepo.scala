package repo

import anorm._
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}

object TestRepo {
  private val dbUrl = "postgres://postgres:postgres@localhost:5432/postgres"
  val db: Database  = Databases(driver = classOf[org.postgresql.Driver].getName, url = dbUrl)

  Evolutions.applyEvolutions(db)

  def truncate(): Int =
    db.withConnection { implicit con =>
      SQL"""TRUNCATE movies CASCADE"""
        .executeUpdate()
    }

  val movieRepo       = new MovieRepo(db)
  val translationRepo = new TranslationRepo(db)
  val questionService = new QuestionService(db, movieRepo)
}
