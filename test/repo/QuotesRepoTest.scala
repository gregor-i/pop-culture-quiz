package repo

import model._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class QuotesRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val movieRepo: MovieRepo  = app.injector.instanceOf[MovieRepo]
  val quotesRepo: QuoteRepo = app.injector.instanceOf[QuoteRepo]

  override def beforeEach(): Unit = {
    movieRepo.list().foreach(row => movieRepo.delete(row.movieId))
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
      Some((480, 482))
    )

  test("insert a new quote") {
    assert(movieRepo.addNewMovie("tt1345836") == 1)
    assert(quotesRepo.addNewQuote(movieId = "tt1345836", quoteId = "qt0333083", quote = quote) == 1)
  }

  test("set the translated quote") {
    assert(movieRepo.addNewMovie("tt1345836") == 1)
    assert(quotesRepo.addNewQuote(movieId = "tt1345836", quoteId = "qt0333083", quote = quote) == 1)
    assert(quotesRepo.setTranslatedQuote("qt0333083", TranslatedQuote(quote, quote, Seq("en", "de", "en"))) == 1)
  }
}
