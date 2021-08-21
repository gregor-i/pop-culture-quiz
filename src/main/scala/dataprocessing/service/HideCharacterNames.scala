package dataprocessing.service

import model.Quote

object HideCharacterNames {
  def apply(quote: Quote): Quote = {
    val transformation = quote.statements
      .flatMap(_.character)
      .distinct
      .zipWithIndex
      .map {
        case (char, index) => (char, s"Person ${index + 1}")
      }
      .toMap

    quote.copy(
      statements = quote.statements.map(statement => statement.copy(character = statement.character.map(transformation)))
    )
  }
}
