package controllers

import com.google.inject.Inject
import play.api.mvc._
import repos.CandidatesRepository

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesController @Inject()(candidatesRepo: CandidatesRepository) extends Controller {
  def all() = Action.async(parse.anyContent) { request =>
    candidatesRepo.findAllCandidates().map { candidates =>
      Ok(candidates.map(_.candidate).mkString(","))
    }
  }
}
