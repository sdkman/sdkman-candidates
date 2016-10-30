package controllers

import javax.inject._

import db.MongoConnectivity
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonString
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthController @Inject()(mongo: MongoConnectivity) extends Controller {

  def alive = Action.async { request =>
    mongo.appCollection.find().first().head.map { doc =>
      extractAlive(doc).fold(NotFound(statusMessage("KO"))) { bs =>
        val message = statusMessage(bs.getValue)
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

  private def extractAlive(doc: Document) = doc.get[BsonString]("alive")

  private def statusMessage(s: String) = Json.obj("status" -> s)

  private def errorMessage(e: Throwable) = Json.obj("status" -> "KO", "error" -> e.getMessage)
}