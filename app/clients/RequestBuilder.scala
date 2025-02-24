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
      platform: String,
      hidden: Boolean = false
  ): WSRequest =
    ws.url(s"$stateApi/versions/$candidate")
      .withQueryStringParameters(("distribution", platform), ("hidden", hidden.toString))
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)
}
