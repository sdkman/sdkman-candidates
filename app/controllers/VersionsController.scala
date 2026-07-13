package controllers

import clients.StateApiImpl
import com.google.inject.Inject
import domain.Platform
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject() (
    stateApi: StateApiImpl,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def all(candidate: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      val universalVersionsF =
        stateApi.findVisibleVersionsByCandidateAndPlatform(candidate, Platform.Universal.name)
      val platformVersionsF =
        stateApi.findVisibleVersionsByCandidateAndPlatform(candidate, Platform(platformId).name)

      for {
        universalVersions <- universalVersionsF
        platformVersions  <- platformVersionsF
      } yield Ok((universalVersions ++ platformVersions).map(_.version).mkString(","))
    }
}
