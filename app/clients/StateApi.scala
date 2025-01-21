package clients

import io.sdkman.repos.Version
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@Singleton
class StateApi @Inject() (ws: WSClient, config: Configuration) {

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

  def findVersionsByCandidateAndDistribution(
      candidate: String,
      distribution: String
  ): Future[Seq[Version]] =
    request(candidate, distribution).get().flatMap { response =>
      response.json.validate[List[Version]] match {
        case JsSuccess(value, _) => Future.successful(value)
        case JsError(e)          => Future.failed(new RuntimeException(e.toString))
      }
    }
}
