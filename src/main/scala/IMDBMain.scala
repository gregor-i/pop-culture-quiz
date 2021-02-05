import imdb.IMDB

import scala.concurrent.Await
import scala.concurrent.duration._

object IMDBMain {
  def main(args: Array[String]): Unit = {
    args.toList match {
      case movieId :: Nil =>
        val quotes = Await.result(IMDB.getMovieQuotes(movieId), 1.minute)
        println("Quotes: ")
        quotes.foreach(println)
      case _ =>
        println("requires a single argument, which is a movie id from imdb. ie. tt0121766")
    }

  }
}
