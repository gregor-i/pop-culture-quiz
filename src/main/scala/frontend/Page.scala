package frontend

import frontend.Frontend.globalContext._
import korolev.web.PathAndQuery

import scala.concurrent.Future
import scala.reflect.ClassTag

// todo: make to trait with scala 3
abstract class Page[S <: FrontendState: ClassTag] {
  def fromState: PartialFunction[FrontendState, PathAndQuery]
  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]]

  def acceptState(frontendState: FrontendState): Boolean =
    implicitly[ClassTag[S]].runtimeClass == frontendState.getClass

  def render(state: S): Node
}

trait NoRouting { self: Page[_] =>
  def fromState: PartialFunction[FrontendState, PathAndQuery]                        = PartialFunction.empty
  def toState: PartialFunction[PathAndQuery, FrontendState => Future[FrontendState]] = PartialFunction.empty
}
