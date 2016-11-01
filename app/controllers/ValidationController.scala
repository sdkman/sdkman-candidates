package controllers

import play.api.libs.json.Json
import play.api.mvc.{Controller, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidationController extends Controller {
  def validate(candidate: String, version: String, platform: String) = Action.async(parse.anyContent) { implicit request =>
    Future {
      render {
        case Accepts.Json() => Ok(Json.obj("valid" -> (if(platform != "freebsd") true else false)))
        case _ => Ok(if (platform != "freebsd") "valid" else "invalid")
      }
    }
  }
}

