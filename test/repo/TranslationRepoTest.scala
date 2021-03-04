package repo

import model.{Quote, TranslatedQuote, TranslationState}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class TranslationRepoTest extends AnyFunSuite with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val movieRepo: MovieRepo              = app.injector.instanceOf[MovieRepo]
  val translationsRepo: TranslationRepo = app.injector.instanceOf[TranslationRepo]

  val quote = Quote(Seq.empty, 1d)

  override def afterEach(): Unit = {
    movieRepo.truncate()
  }

  test("enqueue a quote for translation") {
    assert(movieRepo.addNewMovie("movieId") == 1)
    assert(translationsRepo.enqueue("movieId", "quoteId", quote, "translationService", Seq("de", "fr")) == 1)
    assert(translationsRepo.enqueue("movieId", "quoteId", quote, "translationService", Seq("de", "ar")) == 1)

    assert(translationsRepo.listUnprocessed("translationService").length == 2)

    assert(
      translationsRepo
        .insertTranslatedQuote(
          "movieId",
          "quoteId",
          quote,
          "translationService",
          Seq("de", "fr"),
          TranslationState.Translated(quote)
        ) == 1
    )

    assert(translationsRepo.listUnprocessed("translationService").length == 1)
    assert(
      translationsRepo
        .list()
        .contains(
          TranslationRow(
            movieId = "movieId",
            quoteId = "quoteId",
            quote = quote,
            translationService = "translationService",
            translationChain = Seq("de", "fr"),
            translation = TranslationState.Translated(quote)
          )
        )
    )

    assert(translationsRepo.listUnprocessed("otherService").isEmpty)
  }
}
