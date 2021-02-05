package model

case class TranslatedQuote(original: Quote, translated: Quote, chain: Seq[String])
