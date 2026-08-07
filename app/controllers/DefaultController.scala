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
      case None    => Future.successful(BadRequest(""))
      case Some(c) =>
        // `candidates.distribution` labels are unreliable: some UNIVERSAL-labelled
        // candidates host `lts` only at LINUX_X64 (and vice versa), and the State
        // API filters platform exactly. So on a preferred-platform miss we retry
        // once at the other platform; the preferred result wins when both hit.
        // `java` is excluded — its `lts` needs distribution=TEMURIN and exists at
        // neither platform without it — so it issues exactly one lookup, no fallback.
        val (preferred, fallback, vendor) =
          if (candidate == "java") ("LINUX_X64", None, Some("tem"))
          else if (c.distribution == "UNIVERSAL") ("UNIVERSAL", Some("LINUX_X64"), None)
          else ("LINUX_X64", Some("UNIVERSAL"), None)

        lookup(candidate, preferred, vendor).flatMap {
          case Some(version) => Future.successful(Ok(version.version))
          case None =>
            fallback match {
              case Some(fallbackPlatform) =>
                lookup(candidate, fallbackPlatform, vendor).map {
                  case Some(version) => Ok(version.version)
                  case None          => BadRequest("")
                }
              case None => Future.successful(BadRequest(""))
            }
        }
    }
  }

  private def lookup(candidate: String, platform: String, vendor: Option[String]) =
    stateApi.findVersionByCandidateAndTag(candidate, "lts", platform, vendor)
}
