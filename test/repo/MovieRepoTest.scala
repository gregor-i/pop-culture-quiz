package repo

import model.QuoteCrawlerState
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import java.time.{Instant, ZonedDateTime}

class MovieRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val repo: MovieRepo = app.injector.instanceOf[MovieRepo]

  override def beforeEach(): Unit = {
    repo.truncate()
  }

  test("insert a new movie") {
    assert(repo.addNewMovie("tt1345836") == 1)
    assert(repo.addNewMovie("tt0121766") == 1)
    assert(repo.list().length == 2)
  }

  test("the initial state is NotCrawled") {
    assert(repo.addNewMovie("tt1345836") == 1)

    val movie = repo.get("tt1345836").get
    assert(movie.state == QuoteCrawlerState.NotCrawled)
  }

  test("set crawled as state") {
    assert(repo.addNewMovie("tt1345836") == 1)

    val state = QuoteCrawlerState.Crawled(title = "The Dark Knight Rises", numberOfQuotes = 15, time = ZonedDateTime.now())
    assert(repo.setState("tt1345836", state) == 1)

    val movie = repo.get("tt1345836").get
    assert(movie.state == state)
  }

  test("list movies") {
    assert(repo.addNewMovie("tt1345836") == 1)
    assert(repo.addNewMovie("tt0121766") == 1)

    val state = QuoteCrawlerState.Crawled(title = "The Dark Knight Rises", numberOfQuotes = 15, time = ZonedDateTime.now())
    assert(repo.setState("tt1345836", state) == 1)

    val list = repo.list()
    assert(list.length == 2)

    assert(list.contains(MovieRow(movieId = "tt1345836", state = state)))
    assert(list.contains(MovieRow(movieId = "tt0121766", state = QuoteCrawlerState.NotCrawled)))
  }
}
