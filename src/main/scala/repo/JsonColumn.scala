package repo

import anorm.{Column, SqlMappingError, ToStatement}
import io.circe.{Json, parser}
import org.postgresql.util.PGobject

trait JsonColumn {
  implicit def jsonColumnParser: Column[Option[Json]] =
    Column[Option[Json]] {
      case (obj: PGobject, _) =>
        Right(parser.parse(obj.getValue).toOption)
      case (null, _) =>
        Right(None)
      case other =>
        Left(SqlMappingError("unexpected type"))
    }

  implicit def jsValueToStatement: ToStatement[Json] = ToStatement[Json] { (s, i, json) =>
    val pgObject = new PGobject()
    pgObject.setType("JSONB")
    pgObject.setValue(json.noSpaces)
    s.setObject(i, pgObject, java.sql.Types.OTHER)
  }
}

object JsonColumn extends JsonColumn
