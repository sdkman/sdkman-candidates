package controllers

import com.google.inject.Inject
import play.api.cache.Cached
import play.api.mvc.{Controller, _}
import rendering.{CandidateListSection, PlainTextRendering}
import repos.CandidatesRepo

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesListController @Inject()(candidatesRepo: CandidatesRepo, cached: Cached) extends Controller {

  def list() = cached("candidate-list").default(6.hours) {
    Action.async { request =>
      candidatesRepo.findAllCandidates().map { candidates =>
        val sections = candidates.map { candidate =>
          new CandidateListSection(candidate) with PlainTextRendering
        }
        Ok(views.txt.candidate_list(sections))
      }
    }
  }
}