package controllers

import com.google.inject.Inject
import play.api.mvc.{Controller, _}
import rendering.{CandidateListSection, PlainTextRendering}
import repos.CandidatesRepo

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesListController @Inject()(candidatesRepo: CandidatesRepo) extends Controller {

  def list() = Action.async { _ =>
    candidatesRepo.findAllCandidates().map { candidates =>
      Ok {
        views.txt.candidate_list {
          candidates.map { candidate =>
            new CandidateListSection(candidate) with PlainTextRendering
          }
        }
      }
    }
  }
}