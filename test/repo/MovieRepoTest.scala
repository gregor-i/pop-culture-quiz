package repo

import model.{MovieData, Quote}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class MovieRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val repo: MovieRepo = app.injector.instanceOf[MovieRepo]

  override def afterEach(): Unit = {
    repo.truncate()
  }

  test("insert a new movie") {
    assert(repo.addNewMovie("tt1345836") == 1)
    assert(repo.addNewMovie("tt0121766") == 1)
    assert(repo.list().length == 2)

    val movie = repo.get("tt1345836").get
    assert(movie.data == Left("Not Loaded"))
    assert(movie.quotes.isLeft)
  }

  test("the initial state is null") {
    assert(repo.addNewMovie("tt1345836") == 1)

    assert(repo.listNoData().size == 1)
    assert(repo.listNoQuotes().size == 1)
    assert(repo.list().size == 1)
  }

  test("set movieData") {
    assert(repo.addNewMovie("tt1345836") == 1)

    val movieData =
      MovieData(englishTitle = "The Dark Knight Rises", originalTitle = "wayne", releaseYear = 2152, genre = Set.empty)
    assert(repo.setMovieData("tt1345836", Right(movieData)) == 1)

    val movie = repo.get("tt1345836").get
    assert(movie.data == Right(movieData))
  }

  test("set quotes") {
    assert(repo.addNewMovie("tt1345836") == 1)

    val quotes = Map("1" -> Quote(statements = Seq.empty, score = 1.2), "2" -> Quote(statements = Seq.empty, score = 1.1))
    assert(repo.setQuotes("tt1345836", Right(quotes)) == 1)

    val movie = repo.get("tt1345836").get
    assert(movie.quotes == Right(quotes))
  }

  test("failed result handling") {
    repo.addNewMovie("movieId")
    repo.setMovieData("movieId", Left("error message"))
    assert(repo.get("movieId").get.data == Left("Error: error message"))
  }
}
