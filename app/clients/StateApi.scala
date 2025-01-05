package clients

import io.sdkman.repos.Version
import play.api.libs.ws.{WSClient, WSRequest}
import utils.Platform

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@Singleton
class StateApi @Inject() (ws: WSClient) {

  private def request(candidate: String): WSRequest =
    ws.url(s"https://state.sdkman.io/versions/$candidate")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)

  import play.api.libs.json._
  implicit val versionReads: Reads[Version] = Json.reads[Version]

  def findVersionsByCandidatePlatform(
      candidate: String,
      platform: Platform
  ): Future[Seq[Version]] =
    request(candidate).get().flatMap { response =>
      response.json.validate[List[Version]] match {
        case JsSuccess(value, _) =>
          Future.successful(value.filter(_.platform == platform.distribution))
        case JsError(e) => Future.failed(new RuntimeException(e.toString))
      }
    }
}
