package controllers

import com.google.inject.Inject
import play.api.mvc.{Controller, _}
import repos.VersionsRepo
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidationController @Inject()(versionsRepo: VersionsRepo) extends Controller {

  val Invalid = "invalid"
  val Valid = "valid"

  def validate(candidate: String, version: String, uname: String) = Action.async(parse.anyContent) { implicit request =>
    Platform(uname).map { platform =>
      val maybeUniversalF = versionsRepo.findVersion(candidate, version, Platform.Universal.identifier)
      val maybePlatformSpecificF = versionsRepo.findVersion(candidate, version, platform.identifier)
      for {
        maybeUniversal <- maybeUniversalF
        maybePlatformSpecific <- maybePlatformSpecificF
        maybeVersion = maybeUniversal orElse maybePlatformSpecific
      } yield {
        maybeVersion.fold(Ok(Invalid))(v => Ok(Valid))
      }
    }.getOrElse(Future.successful(Ok(Invalid)))
  }
}