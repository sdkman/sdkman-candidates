package clients

import com.google.inject.Singleton
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt

@Singleton
class RequestBuilder @Inject() (config: Configuration, ws: WSClient) {

  private def stateApiConfig(key: String) = config
    .getOptional[String](s"state-api.$key")
    .getOrElse(throw new RuntimeException("state-api configuration not found"))

  private lazy val stateApi =
    s"${stateApiConfig("protocol")}://${stateApiConfig("host")}:${stateApiConfig("port")}"

  def versionsByCandidatePlatformRequest(
      candidate: String,
      platform: String
  ): WSRequest =
    ws.url(s"$stateApi/versions/$candidate")
      .withQueryStringParameters("platform" -> platform)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)

  def versionByCandidatePlatformRequest(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): WSRequest = {
    val queryParams = List(
      Some("platform" -> platform),
      vendor.map("vendor" -> _)
    ).flatten
    ws.url(s"$stateApi/versions/$candidate/$version")
      .withQueryStringParameters(queryParams: _*)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)
  }
}
