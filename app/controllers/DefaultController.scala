package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import repos.CandidatesRepo

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultController @Inject()(candidatesRepo: CandidatesRepo) extends Controller {
  def find(candidate: String) = Action.async { request =>
    candidatesRepo.findByIdentifier(candidate).map { maybeCandidate =>
      maybeCandidate.fold(BadRequest(""))(c => Ok(c.default))
    }
  }
}
