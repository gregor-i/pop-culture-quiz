package dataprocessing.translation.systran

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class SystranTest extends AnyFunSuite {
  test("simple response") {
    val parsed = parseTestResource("systran_response.json")

    assert(
      parsed == Seq(
        "Ich schreibe etwas Text, damit Sie übersetzen.",
        "Dieses sollte einfacher sein zu übersetzen."
      )
    )
  }

  def parseTestResource(resource: String) = {
    val response = Source.fromResource(resource).mkString("")

    io.circe.parser.decode(response)(SystranTranslate.decoder) match {
      case Right(value) => value
      case Left(value)  => throw value
    }
  }
}
