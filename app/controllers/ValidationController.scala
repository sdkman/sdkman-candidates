package controllers

import com.google.inject.Inject
import play.api.mvc._
import repos.VersionsRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidationController @Inject() (versionsRepo: VersionsRepository, cc: ControllerComponents)
    extends AbstractController(cc) {

  val Invalid = "invalid"
  val Valid   = "valid"

  def validate(candidate: String, version: String, uname: String) = Action.async(parse.anyContent) {
    implicit request =>
      Platform(uname)
        .map { platform =>
          val maybeUniversalF =
            versionsRepo.findVersion(candidate, version, Platform.Universal.identifier)
          val maybePlatformSpecificF =
            versionsRepo.findVersion(candidate, version, platform.identifier)
          for {
            maybeUniversal        <- maybeUniversalF
            maybePlatformSpecific <- maybePlatformSpecificF
            maybeVersion = maybeUniversal orElse maybePlatformSpecific
          } yield {
            maybeVersion.fold(Ok(Invalid))(v => Ok(Valid))
          }
        }
        .getOrElse(Future.successful(Ok(Invalid)))
  }
}
