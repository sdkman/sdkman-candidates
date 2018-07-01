package controllers

import javax.inject.Inject
import ordering.VersionOrdering
import play.api.mvc._
import repos.VersionsRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsListController @Inject()(versionsRepo: VersionsRepository) extends Controller with VersionOrdering {
  def list(candidate: String, platform: String, current: Option[String], installed: List[String]) =
    Action.async(parse.anyContent) { request =>
      //TODO handle platform specific candidates
      versionsRepo.findAllVersionsByCandidatePlatform(candidate, Platform.Universal.identifier).map { versions =>
        Ok(views.txt.version_list(candidate.capitalize, reverseOrder(versions)))
      }
    }
}
