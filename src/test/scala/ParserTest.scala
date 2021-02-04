import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class ParserTest extends AnyFunSuite {
  val starWarsQuotesRaw: String = Source.fromResource("star_wars.html").mkString("")

  test("parse star wars") {
    val parsed = Parser.parse(starWarsQuotesRaw)
    assert(parsed.length == 118)

    val firstQuote = parsed.head
    assert(firstQuote.id == "qt0333083")
    assert(firstQuote.statements.length == 3)

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
