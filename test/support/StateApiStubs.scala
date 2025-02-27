package support

import com.github.tomakehurst.wiremock.client.WireMock._
import domain.Version

import scala.collection.JavaConverters._

object StateApiStubs {

  import play.api.libs.json._
  implicit val versionWrites: Writes[Version] = Json.writes[Version]

  def stubVersionsForCandidateAndPlatform(
      candidate: String,
      platform: String,
      versions: Seq[Version]
  ): Unit =
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate"))
        .withQueryParam("platform", equalTo(platform))
        .willReturn(
          aResponse()
            .withBody(Json.toJson(versions).toString)
            .withStatus(200)
        )
    )

  def stubVersionForCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      url: String,
      vendor: Option[String]
  ): Unit = {
    val queryParams = List(
      Some("platform" -> equalTo(platform)),
      vendor.map(v => "vendor" -> equalTo(v))
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/$version"))
        .withQueryParams(queryParams.toMap.asJava)
        .willReturn(
          aResponse()
            .withBody(
              Json
                .toJson(
                  Version(
                    candidate = candidate,
                    version = version,
                    platform = platform,
                    url = url,
                    vendor = vendor,
                    visible = Some(true)
                  )
                )
                .toString
            )
            .withStatus(200)
        )
    )
  }

  def stubNoVersionForCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): Unit = {
    val queryParams = List(
      Some("platform" -> equalTo(platform)),
      vendor.map("vendor" -> equalTo(_))
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/$version"))
        .withQueryParams(queryParams.toMap.asJava)
        .willReturn(aResponse().withStatus(404))
    )
  }
}
