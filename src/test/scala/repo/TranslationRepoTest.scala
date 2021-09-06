package repo

import model.{Quote, SpeechState, TranslationState}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class TranslationRepoTest extends AnyFunSuite with BeforeAndAfterEach {

  val movieRepo: MovieRepo              = TestRepo.movieRepo
  val translationsRepo: TranslationRepo = TestRepo.translationRepo

  val quote = Quote(Seq.empty, 1d)

  override def afterEach(): Unit = {
    TestRepo.truncate()
  }

  test("enqueue a quote for translation") {
    assert(movieRepo.addNewMovie("movieId") == 1)
    val id = translationsRepo.enqueue("movieId", "quoteId", quote, "translationService", Seq("de", "fr"))
    translationsRepo.enqueue("movieId", "quoteId", quote, "translationService", Seq("de", "ar"))

    assert(translationsRepo.listWithoutTranslation("translationService").length == 2)
    assert(translationsRepo.listWithoutSpeech().isEmpty)

    assert(translationsRepo.setTranslationState(id, TranslationState.Translated(quote)) == 1)
    assert(translationsRepo.listWithoutTranslation("translationService").length == 1)
    assert(translationsRepo.list().exists(_.id == id))
    assert(translationsRepo.listWithoutTranslation("otherService").isEmpty)
    assert(translationsRepo.listWithoutSpeech().map(_.id) == Seq(id))

    assert(translationsRepo.setSpeechState(id, SpeechState.Processed("some data url")) == 1)
    assert(translationsRepo.list().exists(_.id == id))
    assert(translationsRepo.listWithoutSpeech().isEmpty)
  }

}
