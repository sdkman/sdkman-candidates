package controllers

import com.google.inject.Inject
import play.api.mvc._
import repos.VersionsRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject()(versionsRepo: VersionsRepository) extends Controller {
  def all(candidate: String, uname: String) = Action.async(parse.anyContent) { request =>
    val platform = Platform(uname).getOrElse(Platform.Universal)
    versionsRepo.findAllVersions(candidate, platform.identifier).map { versions =>
      Ok(versions.map(_.version).mkString(","))
    }
  }
}
