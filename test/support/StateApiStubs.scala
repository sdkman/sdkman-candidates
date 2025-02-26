package support

import com.github.tomakehurst.wiremock.client.WireMock._
import io.sdkman.repos.Version

object StateApiStubs {

  import play.api.libs.json._
  implicit val versionWrites: Writes[Version] = Json.writes[Version]

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
}
