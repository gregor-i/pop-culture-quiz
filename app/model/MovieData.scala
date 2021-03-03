package model

import io.circe.Codec

case class MovieData(
    englishTitle: String,
    originalTitle: String,
    releaseYear: Int,
    genre: Set[String]
)

object MovieData {
  implicit val codec: Codec[MovieData] = io.circe.generic.semiauto.deriveCodec[MovieData]
}
