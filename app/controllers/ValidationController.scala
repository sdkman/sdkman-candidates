package controllers

import com.google.inject.Inject
import play.api.mvc.{Controller, _}
import repos.VersionsRepo

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationController @Inject()(versionsRepo: VersionsRepo) extends Controller {
  def validate(candidate: String, version: String, platform: String) = Action.async(parse.anyContent) { implicit request =>
    versionsRepo.findVersion(candidate, version, platform.toUpperCase).map { maybeVersion =>
      maybeVersion.fold(NotFound("invalid"))(v => Ok("valid"))
    }
  }
}