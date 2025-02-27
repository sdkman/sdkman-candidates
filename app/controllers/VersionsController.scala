package controllers

import clients.StateApiImpl
import com.google.inject.Inject
import play.api.mvc._
import repos.CandidatesRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject() (
    stateApi: StateApiImpl,
    candidatesRepo: CandidatesRepository,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def all(candidate: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      candidatesRepo.findCandidate(candidate).flatMap { candidateO =>
        val platform = candidateO
          .map(_.distribution)
          .filter(_ == "UNIVERSAL")
          .getOrElse(Platform(platformId).name)

        stateApi.findVisibleVersionsByCandidateAndPlatform(candidate, platform).map { versions =>
          Ok(versions.map(_.version).mkString(","))
        }
      }
    }
}
