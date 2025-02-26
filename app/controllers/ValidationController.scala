package controllers

import clients.StateApiImpl
import com.google.inject.Inject
import play.api.mvc._
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationController @Inject() (
    stateApi: StateApiImpl,
    cc: ControllerComponents
) extends AbstractController(cc) {

  private val Invalid = "invalid"
  private val Valid   = "valid"

  def validate(candidate: String, versionVendor: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      val versionParts = versionVendor.split("-")
      val version = versionParts(0)
      val maybeVendor = versionParts.lift(1)
      val maybeUniversalF =
        stateApi.findVersionByCandidateAndPlatform(
          candidate,
          version,
          Platform.Universal.distribution,
          maybeVendor
        )
      val maybePlatformSpecificF =
        stateApi.findVersionByCandidateAndPlatform(
          candidate,
          version,
          Platform(platformId).distribution,
          maybeVendor
        )
      for {
        maybeUniversal        <- maybeUniversalF
        maybePlatformSpecific <- maybePlatformSpecificF
        maybeVersion = maybeUniversal orElse maybePlatformSpecific
      } yield {
        maybeVersion.fold(Ok(Invalid))(v => Ok(Valid))
      }
    }
}
