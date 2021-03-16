package model

import io.circe.Codec

case class Question(
    correctMovie: MovieData,
    otherMovies: Seq[MovieData],
    originalQuote: Quote,
    translatedQuote: Quote,
    spokenQuoteDataUrl: String
)

object Question {
  implicit val codec: Codec[Question] = io.circe.generic.semiauto.deriveCodec[Question]
}
