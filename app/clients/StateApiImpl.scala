package clients

import io.sdkman.repos.Version
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

trait StateApi {
  def findVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]]
}

@Singleton
class StateApiImpl @Inject() (ws: WSClient, config: Configuration) extends StateApi {

  private def stateApiConfig(key: String) = config
    .getOptional[String](s"state-api.$key")
    .getOrElse(throw new RuntimeException("state-api configuration not found"))

  private lazy val stateApi =
    s"${stateApiConfig("protocol")}://${stateApiConfig("host")}:${stateApiConfig("port")}"

  private def request(candidate: String, platform: String): WSRequest =
    ws.url(s"$stateApi/versions/$candidate/$platform")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)

  import play.api.libs.json._
  implicit val versionReads: Reads[Version] = Json.reads[Version]

  def findVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]] =
    request(candidate, platform).get().flatMap { response =>
      response.json.validate[List[Version]] match {
        case JsSuccess(value, _) => Future.successful(value)
        case JsError(e)          =>
          // TODO: improve error handling
          Future.failed(new RuntimeException(e.toString))
      }
    }
}
