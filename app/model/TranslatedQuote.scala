package model

import io.circe.Codec

case class TranslatedQuote(original: Quote, translated: Quote, chain: Seq[String])

object TranslatedQuote {
  implicit val codec: Codec[TranslatedQuote] = io.circe.generic.semiauto.deriveCodec[TranslatedQuote]
}
