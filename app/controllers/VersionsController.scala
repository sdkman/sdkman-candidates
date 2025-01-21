package controllers

import clients.StateApi
import com.google.inject.Inject
import play.api.mvc._
import repos.CandidatesRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject() (
    stateApi: StateApi,
    candidatesRepo: CandidatesRepository,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def all(candidate: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { request =>
      candidatesRepo.findCandidate(candidate).flatMap { candidateO =>
        val distribution = candidateO
          .map(_.distribution)
          .filter(_ == "UNIVERSAL")
          .getOrElse(Platform(platformId).distribution)

        stateApi.findVersionsByCandidateAndDistribution(candidate, distribution).map { versions =>
          Ok(versions.map(_.version).mkString(","))
        }
      }
    }
}
