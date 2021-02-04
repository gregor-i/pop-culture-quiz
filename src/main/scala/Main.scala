import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}
import sttp.model.Uri

object Main {
  def quotesUrl(title: String) = s"https://www.imdb.com/title/${title}/quotes/"

  val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  def getQuotes(title: String): Option[String] = {
    import sttp.client3._
    basicRequest
      .get(Uri.parse(quotesUrl(title)).getOrElse(???))
      .response(asString)
      .send(backend = backend)
      .body
      .toOption
  }

  def main(args: Array[String]): Unit = {
    args.toList match {
      case movieId :: Nil =>
        getQuotes(movieId) match {
          case Some(rawHtml) =>
            val quotes = Parser.parse(rawHtml)
            println("Quotes: ")
            quotes.foreach(println)
          case None =>
            println("error getting a response from imdb")
        }
      case _ =>
        println("requires a single argument, which is a movie id from imdb. ie. tt0121766")
    }

  }
}
