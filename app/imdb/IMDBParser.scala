package imdb

import model.{Blocking, Quote, Speech, Statement}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.{Element, ElementNode, TextNode}

object IMDBParser {
  def extractTitle(rawHtml: String): String = {
    JsoupBrowser().parseString(rawHtml).body.select(".subpage_title_block .parent a").head.text
  }

  def extractQuotes(rawHtml: String): Map[String, Quote] = {
    (for {
      quoteElement <- JsoupBrowser().parseString(rawHtml).body.select(".quote")
    } yield {
      val id = quoteElement.attr("id")
      val votes = quoteElement.select(".interesting-count-text").map(_.text.trim).collectFirst {
        case s"${upvotes} of ${votes} found this interesting" => (upvotes.toInt, votes.toInt)
      }
      val statements = quoteElement.select(".sodatext").head.children.map { p =>
        val character = p.select(".character").headOption.map(_.text)
        val items = p.childNodes.flatMap {
          case TextNode(content)                    => Some(Speech(content)).flatMap(sanitise)
          case ElementNode(e) if isClass(e, "fine") => Some(Blocking(e.text))
          case _                                    => None
        }.toSeq
        Statement(character = character, items = items)
      }

      id -> Quote(statements.toSeq, votes)
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
