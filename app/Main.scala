import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import translation.google.GoogleTranslate
import imdb.IMDB
import model.TranslatedQuote
import translation.TranslationChain
import translation.systran.SystranTranslate

import java.io.FileWriter
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Using

object Main {
  def main(args: Array[String]): Unit = {
    implicit val as: ActorSystem      = ActorSystem()
    implicit val ex: ExecutionContext = as.dispatcher

//    val fut = TranslationChain(texts = args.toList)
//
//    println(Await.result(fut, Duration.Inf))
//
//
    val starWarsId = "tt0121766"

    println {
      await {
        TranslationChain(
          Seq(
            "I'm writing some text for you to translate.",
            "This should be easier to translate."
          ),
          service = SystranTranslate
        )
      }
    }

    //    val translatedQuotes = Await.result(
//    for {
//      quotes <- IMDB.getMovieQuotes(starWarsId)
//      translatedQuotes <- Future.sequence(quotes.map(TranslateQuote(_)))
//      _ <- as.terminate()
//    } yield translatedQuotes
//    , Duration.Inf)
//    writeToFile(translatedQuotes, s"translated-quotes.$starWarsId.json")

    as.terminate()
  }

  def await[T](fut: Future[T]): T = Await.result(fut, Duration.Inf)

  def writeToFile(translatedQuotes: Seq[TranslatedQuote], file: String): Unit = {
    import io.circe.generic.auto._
    import io.circe.syntax._

    Using(new FileWriter(file)) { fw =>
      fw.append(translatedQuotes.asJson.spaces2)
    }
  }
}
