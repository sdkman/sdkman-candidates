package controllers

import clients.CandidateRegistry
import com.google.inject.Inject
import play.api.mvc._
import rendering.{CandidateListSection, PlainTextRendering}

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesListController @Inject() (
    registry: CandidateRegistry,
    cc: ControllerComponents
) extends AbstractController(cc) {

  // Sections render in the order the State API returns them. The java record
  // never carries a `default`; its header takes the cached Temurin lts
  // identifier instead (specs/candidate-registry-read-flip.md).
  def list(): Action[AnyContent] = Action.async { _ =>
    for {
      candidates  <- registry.candidates()
      javaDefault <- registry.javaDefault()
    } yield candidates match {
      case Some(cs) =>
        Ok {
          views.txt.candidate_list {
            cs.map { c =>
              val candidate = if (c.candidate == "java") c.copy(default = javaDefault) else c
              new CandidateListSection(candidate) with PlainTextRendering
            }
          }
        }
      case None => ServiceUnavailable
    }
  }
}
