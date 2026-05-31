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

  // Callers pass the internal vendor `shortcode` (per
  // specs/vendor-distribution-translation.md). Translate to the State API's
  // wire-side `distribution` enum name at the boundary. An orphan shortcode
  // with no wire counterpart yields no `distribution` query parameter at all —
  // best-effort lookup, since no versions are hosted for defunct distributions.
  private def distributionParam(vendor: Option[String]): Option[(String, String)] =
    vendor.flatMap(VendorDistribution.toDistribution).map("distribution" -> _)

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
      distributionParam(vendor)
    ).flatten
    ws.url(s"$stateApi/versions/$candidate/$version")
      .withQueryStringParameters(queryParams: _*)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)
  }

  def versionByCandidateTagRequest(
      candidate: String,
      tag: String,
      platform: String,
      vendor: Option[String]
  ): WSRequest = {
    val queryParams = List(
      Some("platform" -> platform),
      distributionParam(vendor)
    ).flatten
    ws.url(s"$stateApi/versions/$candidate/tags/$tag")
      .withQueryStringParameters(queryParams: _*)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(1500.millis)
  }
}
