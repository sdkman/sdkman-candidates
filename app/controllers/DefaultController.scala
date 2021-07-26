package controllers

import com.google.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import repos.CandidatesRepository

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultController @Inject()(candidatesRepo: CandidatesRepository, cc: ControllerComponents) extends AbstractController(cc) {
  def find(candidate: String) = Action.async(parse.anyContent) { request =>
    candidatesRepo.findCandidate(candidate).map { maybeCandidate =>
      maybeCandidate.flatMap(c => c.default).fold(BadRequest(""))(default => Ok(default))
    }
  }
}
