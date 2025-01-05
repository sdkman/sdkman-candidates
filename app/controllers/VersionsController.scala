package controllers

import clients.StateApi
import com.google.inject.Inject
import play.api.mvc._
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsController @Inject() (stateApi: StateApi, cc: ControllerComponents)
    extends AbstractController(cc) {

  def all(candidate: String, platformId: String): Action[AnyContent] =
    Action.async(parse.anyContent) { request =>
      stateApi.findVersionsByCandidatePlatform(candidate, Platform(platformId)).map { versions =>
        Ok(versions.map(_.version).mkString(","))
      }
    }
}
