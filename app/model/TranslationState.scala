package model

import io.circe.Codec

sealed trait TranslationState

object TranslationState {
  case object NotTranslated                   extends TranslationState
  case class Translated(quote: Quote)         extends TranslationState
  case class UnexpectedError(message: String) extends TranslationState

  implicit val codec: Codec[TranslationState] = io.circe.generic.semiauto.deriveCodec[TranslationState]
}
