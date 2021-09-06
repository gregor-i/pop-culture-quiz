package di

import akka.stream.Materializer
import com.typesafe.config.Config
import play.api.Mode
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}
import repo.{MovieRepo, QuestionService, TranslationRepo}

class Repo(config: Config)(implicit mat: Materializer) {
  private val dbUrl = config.getString("db.default.url")
  val db: Database  = Databases(driver = classOf[org.postgresql.Driver].getName, url = dbUrl)

  def setupSchema(): Unit =
    Evolutions.applyEvolutions(db)

  val movieRepo       = new MovieRepo(db)
  val translationRepo = new TranslationRepo(db)
  val questionService = new QuestionService(db, movieRepo)
}
