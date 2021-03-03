package model

case class MovieData (
    englishTitle: String,
    originalTitle: String,
    releaseYear: Int,
    genre: Set[String]
                     )
