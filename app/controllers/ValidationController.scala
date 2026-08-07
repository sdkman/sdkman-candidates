package controllers

import clients.StateApiImpl
import com.google.inject.Inject
import domain.Platform
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationController @Inject() (
    stateApi: StateApiImpl,
    cc: ControllerComponents
) extends AbstractController(cc) {

  private val Invalid = "invalid"
  private val Valid   = "valid"

  def validate(candidate: String, versionVendor: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      // The `versionVendor` path segment carries an optional vendor suffix only
      // for java (e.g. `21.0.5-tem`), where the vendor is the tail after the
      // *last* `-`. For every other candidate the dash is part of the version
      // itself (`groovy 6.0.0-alpha-1`, `sbt 2.0.0-RC13`, `kotlin 1.0.5-2`), so
      // the whole segment is the version and there is no vendor.
      val (version, maybeVendor) =
        if (candidate == "java" && versionVendor.contains("-")) {
          val idx = versionVendor.lastIndexOf("-")
          (versionVendor.substring(0, idx), Some(versionVendor.substring(idx + 1)))
        } else
          (versionVendor, None)
      val maybeUniversalF =
        stateApi.findVersionByCandidateAndPlatform(
          candidate,
          version,
          Platform.Universal.name,
          maybeVendor
        )
      val maybePlatformSpecificF =
        stateApi.findVersionByCandidateAndPlatform(
          candidate,
          version,
          Platform(platformId).name,
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
