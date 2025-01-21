package support

import com.github.tomakehurst.wiremock.client.WireMock._
import io.sdkman.repos.Version

object StateApiStubs {

  import play.api.libs.json._
  implicit val versionWrites: Writes[Version] = Json.writes[Version]

  def stubVersions(candidate: String, distribution: String, versions: Seq[Version]): Unit =
    stubFor(
      get(urlEqualTo(s"/versions/$candidate/$distribution"))
        .willReturn(
          aResponse()
            .withBody(Json.toJson(versions).toString)
            .withStatus(200)
        )
    )
}
