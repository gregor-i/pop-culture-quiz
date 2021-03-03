package repo

import model._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class QuotesRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val movieRepo: MovieRepo  = app.injector.instanceOf[MovieRepo]
  val quotesRepo: QuoteRepo = app.injector.instanceOf[QuoteRepo]

  override def beforeEach(): Unit = {
    movieRepo.truncate()
  }

  val quote: Quote =
    Quote(
      List(
        Statement(
          Some("Obi-Wan"),
          List(
            Speech(
              "You were the chosen one! It was said that you would destroy the Sith, not join them! Bring balance to the force... not leave it in darkness!"
            )
          )
        ),
        Statement(Some("Anakin Skywalker"), List(Blocking("shouts"), Speech("I HATE YOU!"))),
        Statement(Some("Obi-Wan"), List(Speech("You were my brother, Anakin! I loved you!")))
      ),
      Score.score(480, 482)
    )

  test("insert a new quote") {
    assert(movieRepo.addNewMovie("tt1345836") == 1)
    assert(quotesRepo.addNewQuote(movieId = "tt1345836", quoteId = "qt0333083", quote = quote) == 1)
    assert(quotesRepo.list() == Seq(QuoteRow(movieId = "tt1345836", quoteId = "qt0333083", quote = quote)))
  }
}
