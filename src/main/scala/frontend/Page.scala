package frontend

import frontend.Frontend.globalContext._

import scala.concurrent.{ExecutionContext, Future}

trait Page[S <: FrontendState] {
  def load(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState]
  def render(state: S): Node
}
