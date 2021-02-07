package model

import io.circe.Codec
import io.circe.generic.auto._

case class Quote(statements: Seq[Statement], count: Option[(Int, Int)])
case class Statement(character: Option[String], items: Seq[Item])

sealed trait Item
case class Blocking(text: String) extends Item
case class Speech(text: String)   extends Item

object Quote {
  def empty = Quote(statements = Seq.empty, count = None)

  implicit val codec: Codec[Quote] = io.circe.generic.semiauto.deriveCodec[Quote]
}
