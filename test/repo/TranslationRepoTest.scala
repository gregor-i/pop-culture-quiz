package repo

import model.{Quote, TranslatedQuote, TranslationState}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class TranslationRepoTest extends AnyFunSuite with GuiceOneAppPerSuite {

  val movieRepo: MovieRepo              = app.injector.instanceOf[MovieRepo]
  val quotesRepo: QuoteRepo             = app.injector.instanceOf[QuoteRepo]
  val translationsRepo: TranslationRepo = app.injector.instanceOf[TranslationRepo]

  val quote = Quote(Seq.empty, 1d)

  test("enqueue a quote for translation") {
    assert(movieRepo.addNewMovie("movieId") == 1)
    assert(quotesRepo.addNewQuote(movieId = "movieId", quoteId = "quoteId", quote = quote) == 1)

    assert(translationsRepo.enqueue("quoteId", "translationService", Seq("de", "fr")) == 1)
    assert(translationsRepo.enqueue("quoteId", "translationService", Seq("de", "ar")) == 1)

    assert(translationsRepo.listUnprocessed("translationService").length == 2)

    assert(
      translationsRepo
        .insertTranslatedQuote("quoteId", "translationService", Seq("de", "fr"), TranslationState.Translated(quote)) == 1
    )

    assert(translationsRepo.listUnprocessed("translationService").length == 1)
    assert(
      translationsRepo
        .list()
        .map(_._1)
        .contains(
          TranslationRow(
            quoteId = "quoteId",
            translationService = "translationService",
            translationChain = Seq("de", "fr"),
            translation = TranslationState.Translated(quote)
          )
        )
    )

    assert(translationsRepo.listUnprocessed("otherService").isEmpty)
  }
}
