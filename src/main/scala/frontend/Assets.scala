package frontend

import akka.http.scaladsl.server.Route
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.pathPrefix

object Assets {
  val routes: Route = pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }
}
