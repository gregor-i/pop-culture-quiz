package imdb

import model.{Blocking, Speech, Statement}
import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class IMDBParserTest extends AnyFunSuite {
  val starWarsQuotesRaw: String = Source.fromResource("star_wars.html").mkString("")

  test("parse star wars title") {
    val parsed = IMDBParser.extractTitle(starWarsQuotesRaw)
    assert(parsed == "Star Wars: Episode III - Die Rache der Sith")
  }

  test("parse star wars quotes") {
    val parsed = IMDBParser.extractQuotes(starWarsQuotesRaw)
    assert(parsed.size == 118)

    val firstQuote = parsed("qt0333083")
    assert(firstQuote.statements.length == 3)
    assert(firstQuote.count.contains((480, 482)))

    val firstStatement = firstQuote.statements.head
    assert(
      firstStatement == Statement(
        character = Some("Obi-Wan"),
        items = Seq(
          Speech(
            "You were the chosen one! It was said that you would destroy the Sith, not join them! Bring balance to the force... not leave it in darkness!"
          )
        )
      )
    )

    val secondStatement = firstQuote.statements(1)
    assert(
      secondStatement == Statement(
        character = Some("Anakin Skywalker"),
        items = Seq(
          Blocking("shouts"),
          Speech("I HATE YOU!")
        )
      )
    )
  }
}
