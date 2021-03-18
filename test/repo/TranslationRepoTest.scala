package repo

import model.{Quote, SpeechState, TranslatedQuote, TranslationState}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.util.Random

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

    assert(translationsRepo.listWithoutTranslation("translationService").length == 2)
    assert(translationsRepo.listWithoutSpeech().isEmpty)

    val rowWithTranslation = translationsRepo
      .list()
      .find(_.translationChain == Seq("de", "fr"))
      .get
      .copy(translation = TranslationState.Translated(quote))

    assert(translationsRepo.upsert(rowWithTranslation) == 1)
    assert(translationsRepo.listWithoutTranslation("translationService").length == 1)
    assert(translationsRepo.list().contains(rowWithTranslation))
    assert(translationsRepo.listWithoutTranslation("otherService").isEmpty)
    assert(translationsRepo.listWithoutSpeech() == Seq(rowWithTranslation))

    val rowWithSpeech = rowWithTranslation.copy(speech = SpeechState.Processed("some data url"))
    assert(translationsRepo.upsert(rowWithSpeech) == 1)
    assert(translationsRepo.list().contains(rowWithSpeech))
    assert(translationsRepo.listWithoutSpeech().isEmpty)
  }

}
