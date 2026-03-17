package controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import repos.ApplicationRepository

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthController @Inject() (appRepo: ApplicationRepository, cc: ControllerComponents)
    extends AbstractController(cc)
    with Logging {

  def alive: Action[AnyContent] = Action.async { request =>
    appRepo
      .findApplication()
      .map { maybeApp =>
        maybeApp.fold(NotFound(statusMessage("KO"))) { app =>
          val message = statusMessage(app.alive)
          logger.info(s"/alive 200 response: $message")
          Ok(message)
        }
      }
      .recover { case e =>
        val message = errorMessage(e)
        logger.error(s"/alive 503 response $message")
        ServiceUnavailable(message)
      }
  }

  def ping: Action[AnyContent] = Action { _ =>
    Ok("000000000000000000000000")
  }

  private def statusMessage(s: String) = Json.obj("status" -> s)

  private def errorMessage(e: Throwable) = Json.obj("status" -> "KO", "error" -> e.getMessage)
}
