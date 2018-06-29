package controllers

import play.api.mvc._

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsListController extends Controller {
  def list(candidate: String, platform: String, current: Option[String], installed: List[String]) =
    Action.async(parse.anyContent) { request =>
      Future(Ok(views.txt.version_list()))
    }
}
