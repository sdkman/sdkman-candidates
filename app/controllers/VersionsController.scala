package controllers

import com.google.inject.Inject
import play.api.mvc._
import repos.{CandidatesRepository, VersionsRepository}
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject()(versionsRepo: VersionsRepository, candidatesRepo: CandidatesRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def all(candidate: String, uname: String) = Action.async(parse.anyContent) { request =>
    candidatesRepo.findCandidate(candidate).flatMap { candidateO =>

      val universal = candidateO.map(_.distribution).contains("UNIVERSAL")
      val platform = if (universal) Platform.Universal else Platform(uname).getOrElse(Platform.Universal)

      versionsRepo.findAllVersionsByCandidatePlatform(candidate, platform.identifier).map { versions =>
        Ok(versions.map(_.version).mkString(","))
      }
    }
  }
}