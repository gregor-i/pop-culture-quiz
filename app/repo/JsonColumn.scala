package repo

import anorm.{Column, ToStatement}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, parser}

trait JsonColumn {
  implicit def jsonColumnParser[T: Decoder]: Column[Either[io.circe.Error, T]] =
    Column.columnToString.map(parser.decode[T])

  implicit def jsonParameterValue: ToStatement[Json] =
    ToStatement.of[String].contramap(_.noSpaces)
}

object JsonColumn extends JsonColumn
