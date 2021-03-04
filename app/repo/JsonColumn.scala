package repo

import anorm.{Column, SqlMappingError, ToStatement}
import io.circe.{Json, parser}
import org.postgresql.util.PGobject

trait JsonColumn {
  implicit def jsonColumnParser: Column[Json] =
    Column[Json] {
      case (obj: PGobject, _) =>
        parser.parse(obj.getValue).left.map(_ => SqlMappingError("invalid json"))
      case (null, _) =>
        Right(Json.Null)
      case other =>
        Left(SqlMappingError("unexpected type"))
    }

  implicit def jsValueToStatement = ToStatement[Json] { (s, i, json) =>
    val pgObject = new PGobject()
    pgObject.setType("JSONB")
    pgObject.setValue(json.noSpaces)
    s.setObject(i, pgObject, java.sql.Types.OTHER)
  }
}

object JsonColumn extends JsonColumn
