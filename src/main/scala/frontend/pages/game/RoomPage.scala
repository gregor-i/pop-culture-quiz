package frontend.pages.game

import frontend.Frontend.globalContext._
import frontend.pages.Common
import frontend.{FrontendState, Page, RoomState}
import korolev.web.PathAndQuery
import korolev.web.PathAndQuery.{/, Root}
import levsha.dsl._
import levsha.dsl.html._

import scala.concurrent.Future

class RoomPage extends Page[RoomState] {
  override def fromState: PartialFunction[FrontendState, PathAndQuery] = {
    case state: RoomState => Root / "room" / state.id
  }

  override def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = {
    case Root / "room" / roomId => state => Future.successful(RoomState(state.deviceId, roomId))
  }

  override def render(state: RoomState): Node =
    optimize {
      Html(
        Common.head("Pop-Culture-Quiz"),
        body(
          `class` := "container",
          h1(`class` := "title", "Pop-Culture-Quiz"),
          h2(`class` := "title", s"Room ${state.id}, ${state.deviceId}")
        )
      )
    }
}
