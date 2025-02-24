package controllers

import com.google.inject.Inject
import play.api.mvc._
import repos.VersionsRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationController @Inject() (versionsRepo: VersionsRepository, cc: ControllerComponents)
    extends AbstractController(cc) {

  private val Invalid = "invalid"
  private val Valid   = "valid"

  def validate(candidate: String, version: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { implicit request =>
      //TODO: Cut over to StateApi
      val maybeUniversalF =
        versionsRepo.findVersion(candidate, version, "UNIVERSAL")
      //TODO: Cut over to StateApi
      val maybePlatformSpecificF =
        versionsRepo.findVersion(candidate, version, Platform(platformId).distribution)
      for {
        maybeUniversal        <- maybeUniversalF
        maybePlatformSpecific <- maybePlatformSpecificF
        maybeVersion = maybeUniversal orElse maybePlatformSpecific
      } yield {
        maybeVersion.fold(Ok(Invalid))(v => Ok(Valid))
      }
    }
}
