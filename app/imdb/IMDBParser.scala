package imdb

import model.{Blocking, MovieData, Quote, Score, Speech, Statement}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.{Element, ElementNode, TextNode}

object IMDBParser {
  def extractTitle(rawHtml: String): String = {
    JsoupBrowser().parseString(rawHtml).body.select(".subpage_title_block .parent a").head.text
  }

  def parseMoviePage(rawHtml: String): Option[MovieData] = {
    val metaTitleRegex = "(.*) \\((\\d*)\\) - IMDb".r

    val document = JsoupBrowser().parseString(rawHtml).root

    val ldJson = document.select("script[type=application/ld+json]").head.innerHtml
    for {
      json          <- io.circe.parser.parse(ldJson).toOption
      originalTitle <- json.asObject.flatMap(_.apply("name")).flatMap(_.as[String].toOption)
      metaTitle = document.select("meta[property='og:title']").head.attr("content")
      Seq(englishTitle, releaseYear) <- metaTitleRegex.unapplySeq(metaTitle)
      genre                          <- json.asObject.flatMap(_.apply("genre")).flatMap(_.as[Set[String]].toOption)
      releaseYear                    <- releaseYear.toIntOption
    } yield {
      MovieData(
        englishTitle = englishTitle,
        originalTitle = originalTitle,
        releaseYear = releaseYear,
        genre = genre
      )
    }
  }

  def extractQuotes(rawHtml: String): Map[String, Quote] = {
    (for {
      quoteElement <- JsoupBrowser().parseString(rawHtml).body.select(".quote")
    } yield {
      val id = quoteElement.attr("id")
      val score = quoteElement
        .select(".interesting-count-text")
        .map(_.text.trim)
        .collectFirst {
          case s"${upvotes} of ${votes} found this interesting" => (upvotes.replace(",", "").toInt, votes.replace(",", "").toInt)
        }
        .fold(1d) { case (upvotes, votes) => Score.score(upvotes, votes) }
      val statements = quoteElement.select(".sodatext").head.children.map { p =>
        val character = p.select(".character").headOption.map(_.text)
        val items = p.childNodes.flatMap {
          case TextNode(content)                    => Some(Speech(content)).flatMap(sanitise)
          case ElementNode(e) if isClass(e, "fine") => Some(Blocking(e.text))
          case _                                    => None
        }.toSeq
        Statement(character = character, items = items)
      }

      id -> Quote(statements.toSeq, score)
    }).toMap
  }

  private def isClass(element: Element, expected: String): Boolean =
    element.attrs.get("class").exists(_.split(" ").contains(expected))

  private def sanitise(speech: Speech): Option[Speech] = {
    def toRemove: Char => Boolean = {
      case ':' => true
      case ' ' => true
      case '[' => true
      case ']' => true
      case _   => false
    }

    val trimmed = speech.text.dropWhile(toRemove).reverse.dropWhile(toRemove).reverse
    if (trimmed.isEmpty)
      None
    else
      Some(Speech(trimmed))
  }
}
