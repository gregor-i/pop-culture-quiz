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

  implicit val ordering: Ordering[Quote] = Ordering.by {
    case Quote(_, Some((upVotes, votes))) => 1d - Score.score(upVotes, votes)
    case _                                => 1d
  }
}

object Score {
  // source:  https://www.evanmiller.org/how-not-to-sort-by-average-rating.html
  def score(pos: Int, n: Int): Double = {
    val z    = 1.96
    val phat = pos.toDouble / n
    (phat + z * z / (2.0 * n) - z * Math.sqrt((phat * (1.0 - phat) + z * z / (4.0 * n)) / n)) / (1.0 + z * z / n)
  }
}
