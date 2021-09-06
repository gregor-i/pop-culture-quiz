package model

import io.circe.Codec

sealed trait SpeechState

object SpeechState {
  case object NotProcessed                    extends SpeechState
  case class Processed(base64Data: String)    extends SpeechState
  case class UnexpectedError(message: String) extends SpeechState

  implicit val codec: Codec[SpeechState] = io.circe.generic.semiauto.deriveCodec[SpeechState]
}
