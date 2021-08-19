import korolev.Context
import scala.concurrent.Future
import korolev.state.javaSerialization._

sealed trait FrontendState
case class AdminAgentsState(i: Int) extends FrontendState
case object NotFoundState           extends FrontendState
case object LoadingState            extends FrontendState
