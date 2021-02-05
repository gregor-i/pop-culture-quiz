package googleTranslate

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class GoogleTranslateTest extends AnyFunSuite {
  test("decode the response") {
    val translations = parseTestResource("google_response.json")

    assert(translations.size == 2)
    assert(translations.get("hallo welt").contains("Hello World"))
    assert(translations.get("wie geht es dir").contains("How are you"))
  }

  test("decode a response with pronunciation") {
    val translations = parseTestResource("google_response_pronunciation.json")

    assert(translations.size == 1)
    assert(translations.get("This text will travel around the globe").contains("هذا النص سوف يسافر حول العالم"))
  }

  def parseTestResource(resource: String) = {
    val response = Source.fromResource(resource).mkString("")

    io.circe.parser.decode(response)(GoogleTranslate.decoder) match {
      case Right(value) => value
      case Left(value) => throw value
    }
  }
}
