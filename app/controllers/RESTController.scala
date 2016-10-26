package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RESTController @Inject() extends Controller {
  def status = Action.async { request =>
    Future {
      Ok(Json.obj("status" -> "OK"))
    }
  }
}
