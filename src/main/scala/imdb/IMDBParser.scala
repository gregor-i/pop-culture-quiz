package imdb

import model.{Blocking, Quote, Speech, Statement}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.{Element, ElementNode, TextNode}

object IMDBParser {
  def parse(rawHtml: String): Seq[Quote] = {
    (for {
      quoteElement <- JsoupBrowser().parseString(rawHtml).body.select(".quote")
    } yield {
      val id = quoteElement.attr("id")
      val statements = quoteElement.select(".sodatext").head.children.map { p =>
        val character = p.select(".character").headOption.map(_.text)
        val items = p.childNodes.flatMap {
          case TextNode(content)                    => Some(Speech(content)).flatMap(sanitise)
          case ElementNode(e) if isClass(e, "fine") => Some(Blocking(e.text))
          case _                                    => None
        }.toSeq
        Statement(character = character, items = items)
      }

      Quote(id, statements.toSeq)
    }).toList
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
