package repo

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import java.time.Instant

class MovieRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val repo: MovieRepo = app.injector.instanceOf[MovieRepo]

  override def beforeEach(): Unit = {
    repo.list().foreach(row => repo.delete(row.movieId))
  }

  test("insert a new movie") {
    repo.addNewMovie("tt1345836", "The Dark Knight Rises")
    repo.addNewMovie("tt0121766", "Star Wars: Episode III - Die Rache der Sith")
    assert(repo.list().length == 2)
  }

  test("mark a movie as crawled") {
    repo.addNewMovie("tt1345836", "The Dark Knight Rises")
    repo.markMovieAsCrawled("tt1345836")

    val list = repo.list()
    assert(list.length == 1)

    val movieRow = list.head
    assert(movieRow.movieId == "tt1345836")
    assert(movieRow.title == "The Dark Knight Rises")
    assert(movieRow.lastCrawledAt.isDefined)

    val lastCrawledAt = movieRow.lastCrawledAt.get
    assert(Math.abs(lastCrawledAt.toInstant.toEpochMilli - Instant.now().toEpochMilli) < 1000)
  }

  test("list not crawled movies") {
    repo.addNewMovie("tt1345836", "The Dark Knight Rises")
    repo.addNewMovie("tt0121766", "Star Wars: Episode III - Die Rache der Sith")
    repo.markMovieAsCrawled("tt0121766")

    val list = repo.listNotCrawled()
    assert(list.length == 1)

    val movieRow = list.head
    assert(movieRow.movieId == "tt1345836")
  }
}
