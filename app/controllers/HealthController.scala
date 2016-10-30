package controllers

import javax.inject._

import db.MongoConnectivity
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonString
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthController @Inject()(mongo: MongoConnectivity) extends Controller {

  def alive = Action.async { request =>
    mongo.appCollection.find().first().head.map { doc =>
      extractStatus(doc).fold(NotFound(statusMessage("KO"))) { bs =>
        Ok(statusMessage(bs.getValue))
      }
    }.recover {
      case e => ServiceUnavailable(errorMessage(e))
    }
  }

  private def extractStatus(doc: Document) = doc.get[BsonString]("status")

  private def statusMessage(s: String) = Json.obj("status" -> s)

  private def errorMessage(e: Throwable) = Json.obj("status" -> "KO", "error" -> e.getMessage)
}