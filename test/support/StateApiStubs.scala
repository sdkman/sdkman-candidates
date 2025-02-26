package support

import clients.StateApiVersion
import com.github.tomakehurst.wiremock.client.WireMock._
import io.sdkman.repos.{Candidate, Version}

import scala.collection.JavaConverters._

object StateApiStubs {

  import play.api.libs.json._
  implicit val versionWrites: Writes[Version]                 = Json.writes[Version]
  implicit val stateApiVersionWrites: Writes[StateApiVersion] = Json.writes[StateApiVersion]

  def stubVersionsForCandidateAndDistribution(
      candidate: String,
      distribution: String,
      versions: Seq[Version]
  ): Unit =
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate"))
        .withQueryParam("distribution", equalTo(distribution))
        .willReturn(
          aResponse()
            .withBody(Json.toJson(versions).toString)
            .withStatus(200)
        )
    )

  def stubVersionForCandidateAndDistribution(
      candidate: String,
      version: String,
      distribution: String,
      url: String,
      vendor: Option[String]
  ): Unit = {
    val queryParams = List(
      Some("distribution" -> equalTo(distribution)),
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
                  StateApiVersion(
                    candidate = candidate,
                    version = version,
                    distribution = distribution,
                    url = url,
                    vendor = vendor,
                    visible = true
                  )
                )
                .toString
            )
            .withStatus(200)
        )
    )
  }

  def stubNoVersionForCandidateAndDistribution(
      candidate: String,
      version: String,
      distribution: String,
      vendor: Option[String]
  ): Unit = {
    val queryParams = List(
      Some("distribution" -> equalTo(distribution)),
      vendor.map("vendor" -> equalTo(_))
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/$version"))
        .withQueryParams(queryParams.toMap.asJava)
        .willReturn(aResponse().withStatus(404))
    )
  }
}
