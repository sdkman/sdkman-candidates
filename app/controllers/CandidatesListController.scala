package controllers

import com.google.inject.Inject
import play.api.mvc.{Controller, _}
import rendering.{CandidateListSection, PlainTextRendering}
import repos.CandidatesRepo

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesListController @Inject()(candidatesRepo: CandidatesRepo) extends Controller {

  def list = Action.async { request =>
    candidatesRepo.findAllCandidates().map { candidates =>
      val sections = candidates.map { candidate =>
        new CandidateListSection(candidate) with PlainTextRendering
      }
      Ok(views.txt.candidate_list(sections))
    }
  }
}