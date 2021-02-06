package model

case class Quote(id: String, statements: Seq[Statement], count: Option[(Int, Int)])
case class Statement(character: Option[String], items: Seq[Item])

sealed trait Item
case class Blocking(text: String) extends Item
case class Speech(text: String)   extends Item
