package support

import clients.VendorDistribution
import com.github.tomakehurst.wiremock.client.WireMock._
import domain.Version
import utils.JsonConverters

import scala.collection.JavaConverters._

object StateApiStubs extends JsonConverters {

  import play.api.libs.json._

  // Callers pass the internal vendor `shortcode` (per
  // specs/vendor-distribution-translation.md). The WireMock matcher mirrors
  // the real State API wire contract by translating the shortcode to the
  // distribution enum name. Orphan shortcodes have no wire counterpart, so no
  // `distribution` matcher is added — matching the absence on the wire that
  // `RequestBuilder` produces for the same shortcode.
  private def distributionMatcher(vendor: Option[String]) =
    vendor
      .flatMap(VendorDistribution.toDistribution)
      .map(d => "distribution" -> equalTo(d))

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

  // Mirrors a non-2xx from the real State API listing read — e.g. the 400 it
  // returns for platforms it does not recognise (FREE_BSD/SUN_OS, behind
  // `freebsd`/`sunos`), or a 500/503 blip. The client degrades these to an
  // empty listing rather than surfacing a 500.
  def stubVersionsErrorForCandidateAndPlatform(
      candidate: String,
      platform: String,
      status: Int
  ): Unit =
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate"))
        .withQueryParam("platform", equalTo(platform))
        .willReturn(aResponse().withStatus(status))
    )

  // A 200 whose body is not Version[] — contract drift. The client must still
  // fail on this rather than treat it as "no versions".
  def stubMalformedVersionsForCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Unit =
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate"))
        .withQueryParam("platform", equalTo(platform))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody("""{"unexpected":"shape"}""")
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
      distributionMatcher(vendor)
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
      distributionMatcher(vendor)
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/$version"))
        .withQueryParams(queryParams.toMap.asJava)
        .willReturn(aResponse().withStatus(404))
    )
  }

  def stubVersionForCandidateAndTag(
      candidate: String,
      tag: String,
      platform: String,
      vendor: Option[String],
      version: String
  ): Unit = {
    val queryParams = List(
      Some("platform" -> equalTo(platform)),
      distributionMatcher(vendor)
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/tags/$tag"))
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
                    url = s"https://downloads/$candidate/$version/$candidate-$version.tar.gz",
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

  def stubNoVersionForCandidateAndTag(
      candidate: String,
      tag: String,
      platform: String,
      vendor: Option[String]
  ): Unit = {
    val queryParams = List(
      Some("platform" -> equalTo(platform)),
      distributionMatcher(vendor)
    ).flatten
    stubFor(
      get(urlPathEqualTo(s"/versions/$candidate/tags/$tag"))
        .withQueryParams(queryParams.toMap.asJava)
        .willReturn(aResponse().withStatus(404))
    )
  }
}
