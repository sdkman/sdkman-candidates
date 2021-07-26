package controllers

import com.google.inject.Inject
import play.api.mvc._
import rendering.{CandidateListSection, PlainTextRendering}
import repos.CandidatesRepository

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesListController @Inject()(candidatesRepo: CandidatesRepository, cc: ControllerComponents) extends AbstractController(cc) {

  def list() = Action.async { _ =>
    candidatesRepo.findAllCandidates().map { candidates =>
      Ok {
        views.txt.candidate_list {
          candidates.filterNot(c => c.candidate == "test").map { candidate =>
            new CandidateListSection(candidate) with PlainTextRendering
          }
        }
      }
    }
  }
}