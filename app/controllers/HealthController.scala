package controllers

import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import repos.ApplicationRepository

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthController @Inject()(appRepo: ApplicationRepository) extends Controller {

  def alive = Action.async { request =>
    appRepo.findApplication().map { maybeApp =>
      maybeApp.fold(NotFound(statusMessage("KO"))) { app =>
        val message = statusMessage(app.alive)
        Logger.info(s"/alive 200 response: $message")
        Ok(message)
      }
    }.recover {
      case e =>
        val message = errorMessage(e)
        Logger.error(s"/alive 503 response $message")
        ServiceUnavailable(message)
    }
  }

  private def statusMessage(s: String) = Json.obj("status" -> s)

  private def errorMessage(e: Throwable) = Json.obj("status" -> "KO", "error" -> e.getMessage)
}