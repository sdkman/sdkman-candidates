package controllers

import clients.CandidateRegistry
import com.google.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatesController @Inject() (
    registry: CandidateRegistry,
    cc: ControllerComponents
) extends AbstractController(cc) {

  // The State API owns the order; the identifiers are joined as received.
  // With no set, the body stays empty: three clients write it straight into
  // $SDKMAN_DIR/var/candidates (specs/candidate-registry-read-flip.md).
  def all(): Action[AnyContent] = Action.async(parse.anyContent) { _ =>
    registry.candidates().map {
      case Some(candidates) => Ok(candidates.map(_.candidate).mkString(","))
      case None             => ServiceUnavailable
    }
  }
}
