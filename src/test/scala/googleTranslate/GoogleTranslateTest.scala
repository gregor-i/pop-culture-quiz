package googleTranslate

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class GoogleTranslateTest extends AnyFunSuite {
  val response = Source.fromResource("google_response.json").mkString("")

  test("decode the response") {
    val json = io.circe.parser.parse(response).getOrElse(fail())

    val translations = json.as(GoogleTranslate.decoder).getOrElse(fail())

    assert(translations.size == 2)
    assert(translations.get("hallo welt").contains("Hello World"))
    assert(translations.get("wie geht es dir").contains("How are you"))
  }
}
