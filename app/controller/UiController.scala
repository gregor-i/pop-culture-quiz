package controller

import controllers.Assets
import play.api.mvc.InjectedController
import play.api.{Environment, Mode}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UiController @Inject() (assets: Assets, env: Environment)(implicit ex: ExecutionContext) extends InjectedController {
  def frontend(path: String) = asset("index.html", "/public")

  def asset(file: String, folder: String) = assets.at(folder, file)
}
