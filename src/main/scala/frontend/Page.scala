package frontend

import di.{Global, Repo}

import scala.concurrent.{ExecutionContext, Future}
import frontend.Frontend.globalContext._

trait Page[S <: FrontendState] {
  def load(global: Global)(state: FrontendState)(implicit ex: ExecutionContext): Future[FrontendState]
  def render(state: S): Node
}
