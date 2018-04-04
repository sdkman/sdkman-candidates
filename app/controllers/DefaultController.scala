package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import repos.CandidatesRepository

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultController @Inject()(candidatesRepo: CandidatesRepository) extends Controller {
  def find(candidate: String) = Action.async(parse.anyContent) { request =>
    candidatesRepo.findCandidate(candidate).map { maybeCandidate =>
      maybeCandidate.fold(BadRequest(""))(c => Ok(c.default))
    }
  }
}
