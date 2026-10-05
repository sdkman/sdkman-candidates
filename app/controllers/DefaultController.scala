package controllers

import clients.CandidateRegistry
import com.google.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultController @Inject() (
    registry: CandidateRegistry,
    cc: ControllerComponents
) extends AbstractController(cc) {

  // Answered wholly from the registry: no tag lookup, no platform choice. The
  // State API derives each record's `default`; java's is the cached Temurin lts
  // identifier (specs/candidate-registry-read-flip.md, §GET /default/:candidate).
  def find(candidate: String): Action[AnyContent] = Action.async(parse.anyContent) { _ =>
    for {
      candidates  <- registry.candidates()
      javaDefault <- registry.javaDefault()
    } yield candidates match {
      case None => ServiceUnavailable
      case Some(cs) =>
        cs.find(_.candidate == candidate) match {
          case None => BadRequest("")
          case Some(c) =>
            val default = if (candidate == "java") javaDefault else c.default
            default.fold(BadRequest(""))(Ok(_))
        }
    }
  }
}
