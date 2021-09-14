package frontend

import frontend.Frontend.globalContext.Node
import korolev.web.PathAndQuery
import scala.concurrent.Future

trait Page[S <: FrontendState] {
  def fromState: PartialFunction[FrontendState, PathAndQuery]
  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]]

  def render(state: S): Node
}

trait NoRouting { self: Page[_] =>
  def fromState: PartialFunction[FrontendState, PathAndQuery]                        = PartialFunction.empty
  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = PartialFunction.empty
}
