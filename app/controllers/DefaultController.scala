package controllers

import clients.StateApiImpl
import com.google.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import repos.CandidatesRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultController @Inject() (
    stateApi: StateApiImpl,
    candidatesRepo: CandidatesRepository,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def find(candidate: String): Action[AnyContent] = Action.async(parse.anyContent) { _ =>
    candidatesRepo.findCandidate(candidate).flatMap {
      case None => Future.successful(BadRequest(""))
      case Some(c) =>
        val (platform, vendor) =
          if (candidate == "java") ("LINUX_X64", Some("tem"))
          else if (c.distribution == "UNIVERSAL") ("UNIVERSAL", None)
          else ("LINUX_X64", None)
        stateApi.findVersionByCandidateAndTag(candidate, "lts", platform, vendor).map {
          case Some(version) => Ok(version.version)
          case None          => BadRequest("")
        }
    }
  }
}
