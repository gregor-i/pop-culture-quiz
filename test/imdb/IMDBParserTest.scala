package imdb

import model.{Blocking, Score, Speech, Statement}
import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class IMDBParserTest extends AnyFunSuite {
  test("parse movie data (the godfather)") {
    val moviePageRaw = Source.fromResource("movie_page_1.html").mkString("")

    val parsed = IMDBParser.parseMoviePage(moviePageRaw)

    assert(parsed.isDefined)
    assert(parsed.get.englishTitle == "The Godfather")
    assert(parsed.get.originalTitle == "The Godfather")
    assert(parsed.get.genre == Set("Crime", "Drama"))
    assert(parsed.get.releaseYear == 1972)
  }

  test("parse movie data (oldboy)") {
    val moviePageRaw = Source.fromResource("movie_page_2.html").mkString("")

    val parsed = IMDBParser.parseMoviePage(moviePageRaw)

    assert(parsed.isDefined)
    assert(parsed.get.englishTitle == "Oldboy")
    assert(parsed.get.originalTitle == "Oldeuboi")
    assert(parsed.get.genre == Set("Action", "Drama", "Mystery", "Thriller"))
    assert(parsed.get.releaseYear == 2003)
  }

  test("parse star wars quotes") {
    val starWarsQuotesRaw = Source.fromResource("movie_quotes_page.html").mkString("")
    val parsed            = IMDBParser.extractQuotes(starWarsQuotesRaw)
    assert(parsed.size == 118)

    val firstQuote = parsed("qt0333083")
    assert(firstQuote.statements.length == 3)
    assert(firstQuote.score == Score.score(480, 482))

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
