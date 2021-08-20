package frontend

sealed trait FrontendState
case object AdminAgentsState extends FrontendState
case object NotFoundState    extends FrontendState
case object LoadingState     extends FrontendState
