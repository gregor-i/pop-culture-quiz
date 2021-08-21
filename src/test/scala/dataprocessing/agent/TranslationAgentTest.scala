package dataprocessing.agent

import akka.actor.ActorSystem
import akka.stream.Materializer
import model.{Quote, TranslationState}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.funsuite.AnyFunSuite
import repo.{MovieRepo, TestRepo, TranslationRepo}
import dataprocessing.translation.TranslationService

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class TranslationAgentTest extends AnyFunSuite with Eventually with BeforeAndAfterEach {

  val movieRepo: MovieRepo              = TestRepo.movieRepo
  val translationsRepo: TranslationRepo = TestRepo.translationRepo

  val quote = Quote(Seq.empty, 1d)

  override def afterEach() = {
    TestRepo.truncate()
  }

  test("handles translations") {
    val as      = ActorSystem.apply("test")
    val service = new DummyService()
    val agent   = new TranslationAgent(service, translationsRepo)(as, as.dispatcher, Materializer.createMaterializer(as)) {}

    agent.start()

    movieRepo.addNewMovie("movieId")
    translationsRepo.enqueue(
      "movieId",
      "quoteId",
      quote,
      translationService = service.name,
      translationChain = Seq("de", "fr", "nl")
    )

    eventually(timeout = Timeout(1.25.seconds)) {
      assert(translationsRepo.listWithoutTranslation(service.name).isEmpty)
    }

    translationsRepo.list().head.translation match {
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

    agent.start()

    movieRepo.addNewMovie("movieId")
    translationsRepo.enqueue(
      "movieId",
      "quoteId",
      quote,
      translationService = service.name,
      translationChain = Seq("de", "fr", "nl")
    )

    eventually(timeout = Timeout(1.25.seconds)) {
      assert(translationsRepo.listWithoutTranslation(service.name).isEmpty)
    }

    translationsRepo.list().head.translation match {
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
