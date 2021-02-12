package agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import model.{Quote, TranslationState}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.time.Seconds
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import repo.{MovieRepo, QuoteRepo, TranslationRepo}
import translation.TranslationService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class TranslationAgentTest extends AnyFunSuite with GuiceOneAppPerSuite with Eventually with BeforeAndAfterEach {

  val movieRepo: MovieRepo              = app.injector.instanceOf[MovieRepo]
  val quotesRepo: QuoteRepo             = app.injector.instanceOf[QuoteRepo]
  val translationsRepo: TranslationRepo = app.injector.instanceOf[TranslationRepo]

  val quote = Quote(Seq.empty, None)

  override def beforeEach() = {
    movieRepo.truncate()
  }

  test("handles translations") {
    val as      = ActorSystem.apply("test")
    val service = new DummyService()
    val agent   = new TranslationAgent(service, translationsRepo)(as, as.dispatcher, Materializer.createMaterializer(as)) {}

    agent.running = true

    movieRepo.addNewMovie("movieId")
    quotesRepo.addNewQuote(movieId = "movieId", quoteId = "quoteId", quote = quote)
    translationsRepo.enqueue("quoteId", translationService = service.name, translationChain = Seq("de", "fr", "nl"))

    eventually(timeout = Timeout(1.25.seconds)) {
      assert(translationsRepo.listUnprocessed(service.name).isEmpty)
    }

    translationsRepo.list().head._1.translation match {
      case TranslationState.Translated(quote) =>
        assert(quote == this.quote)
      case _ => fail()
    }

    Await.result(as.terminate(), Duration.Inf)
  }

  test("handles exceptions from Translation Service") {
    val as      = ActorSystem.apply("test")
    val service = new FailingDummyService()
    val agent   = new TranslationAgent(service, translationsRepo)(as, as.dispatcher, Materializer.createMaterializer(as)) {}

    agent.running = true

    movieRepo.addNewMovie("movieId")
    quotesRepo.addNewQuote(movieId = "movieId", quoteId = "quoteId", quote = quote)
    translationsRepo.enqueue("quoteId", translationService = service.name, translationChain = Seq("de", "fr", "nl"))

    eventually(timeout = Timeout(1.25.seconds)) {
      assert(translationsRepo.listUnprocessed(service.name).isEmpty)
    }

    translationsRepo.list().head._1.translation match {
      case TranslationState.UnexpectedError(message) =>
        assert(message == "FailingDummyService doin' his thing")
      case _ => fail()
    }

    Await.result(as.terminate(), Duration.Inf)
  }
}

private class DummyService extends TranslationService {
  override def apply(texts: Seq[String], src: String, dest: String)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] =
    Future.successful(texts.map(t => (t, t)).toMap)

  val name                               = "dummy"
  override def defaultChain: Seq[String] = Seq.empty
}

private class FailingDummyService extends TranslationService {
  override def apply(texts: Seq[String], src: String, dest: String)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] =
    Future.failed(new Exception("FailingDummyService doin' his thing"))

  val name                               = "dummy"
  override def defaultChain: Seq[String] = Seq.empty
}
