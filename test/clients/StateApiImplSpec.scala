package clients

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.config.ConfigFactory
import domain.Version
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Inspectors.forAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import utils.JsonConverters

class StateApiImplSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with JsonConverters {

  // Bound on a port distinct from the Cucumber WireMock (8080) so the two
  // can coexist when sbt runs them in the same JVM.
  private val wireMockPort                        = 8089
  private val wireMockServer                      = new WireMockServer(options().port(wireMockPort))
  private implicit val actorSystem: ActorSystem   = ActorSystem("StateApiImplSpec")
  private implicit val materializer: Materializer = Materializer.matFromSystem(actorSystem)
  private val wsClient: WSClient                  = AhcWSClient()

  private val config = Configuration(
    ConfigFactory.parseString(
      s"""state-api {
         |  protocol = "http"
         |  host     = "localhost"
         |  port     = $wireMockPort
         |}""".stripMargin
    )
  )

  private val stateApi = new StateApiImpl(new RequestBuilder(config, wsClient))

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(50, Millis))

  override def beforeAll(): Unit = wireMockServer.start()

  override def afterAll(): Unit = {
    wireMockServer.stop()
    wsClient.close()
    actorSystem.terminate()
  }

  // Drive stubs against this specific server (avoid WireMock's static default,
  // which would target localhost:8080 and clash with the Cucumber server).
  private def stub = wireMockServer.stubFor _

  "findVersionByCandidateAndTag" should {

    "GET /versions/{candidate}/tags/{tag} with platform + distribution and map 200 to Some(Version)" in {
      // Caller-side vendor is the internal shortcode (`tem`); the wire matcher
      // asserts the State API distribution enum name (`TEMURIN`). Both
      // translations — the request-side query parameter and the response-side
      // JSON body — happen at the State API client boundary per
      // specs/vendor-distribution-translation.md.
      val expected = Version(
        candidate = "java",
        version = "21.0.5-tem",
        platform = "LINUX_X64",
        url = "https://downloads/java/21.0.5-tem/java-21.0.5-tem.tar.gz",
        visible = Some(true),
        vendor = Some("tem")
      )

      stub(
        get(urlPathEqualTo("/versions/java/tags/lts"))
          .withQueryParam("platform", equalTo("LINUX_X64"))
          .withQueryParam("distribution", equalTo("TEMURIN"))
          .willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString))
      )

      stateApi
        .findVersionByCandidateAndTag("java", "lts", "LINUX_X64", Some("tem"))
        .futureValue shouldBe Some(expected)
    }

    "omit the distribution query parameter for an orphan vendor shortcode" in {
      // `adpt` (AdoptOpenJDK, superseded by Temurin) has no State API
      // distribution. Per the spec the client treats this as no distribution:
      // the wire request carries no `distribution` parameter, and the lookup
      // is best-effort — there are no versions hosted for defunct distributions
      // so the natural outcome is the State API's no-match.
      stub(
        get(urlPathEqualTo("/versions/java/tags/lts"))
          .withQueryParam("platform", equalTo("LINUX_X64"))
          .withQueryParam("distribution", absent())
          .willReturn(aResponse().withStatus(404))
      )

      stateApi
        .findVersionByCandidateAndTag("java", "lts", "LINUX_X64", Some("adpt"))
        .futureValue shouldBe None
    }

    "omit the distribution query parameter when no vendor is supplied" in {
      val expected = Version(
        candidate = "cuba",
        version = "8.0.0",
        platform = "LINUX_X64",
        url = "https://downloads/cuba/8.0.0/cuba-8.0.0.tar.gz",
        visible = Some(true),
        vendor = None
      )

      stub(
        get(urlPathEqualTo("/versions/cuba/tags/lts"))
          .withQueryParam("platform", equalTo("LINUX_X64"))
          .withQueryParam("distribution", absent())
          .willReturn(aResponse().withStatus(200).withBody(Json.toJson(expected).toString))
      )

      stateApi
        .findVersionByCandidateAndTag("cuba", "lts", "LINUX_X64", None)
        .futureValue shouldBe Some(expected)
    }

    "map a 404 from the State API to None" in {
      stub(
        get(urlPathEqualTo("/versions/missing/tags/lts"))
          .withQueryParam("platform", equalTo("UNIVERSAL"))
          .willReturn(aResponse().withStatus(404))
      )

      stateApi
        .findVersionByCandidateAndTag("missing", "lts", "UNIVERSAL", None)
        .futureValue shouldBe None
    }
  }

  "findVisibleVersionsByCandidateAndPlatform" should {

    def stubListing(candidate: String, platform: String)(
        response: ResponseDefinitionBuilder
    ): Unit =
      stub(
        get(urlPathEqualTo(s"/versions/$candidate"))
          .withQueryParam("platform", equalTo(platform))
          .willReturn(response)
      )

    "map a 200 body to the parsed Version list" in {
      val versions = List(
        Version("groovy", "4.0.0", "UNIVERSAL", "https://dl/groovy/4.0.0.zip", Some(true), None),
        Version("groovy", "4.0.1", "UNIVERSAL", "https://dl/groovy/4.0.1.zip", Some(true), None)
      )
      stubListing("groovy", "UNIVERSAL")(
        aResponse().withStatus(200).withBody(Json.toJson(versions).toString)
      )

      stateApi
        .findVisibleVersionsByCandidateAndPlatform("groovy", "UNIVERSAL")
        .futureValue shouldBe versions
    }

    "map a 200 empty array to an empty Seq" in {
      stubListing("groovy", "UNIVERSAL")(aResponse().withStatus(200).withBody("[]"))

      stateApi
        .findVisibleVersionsByCandidateAndPlatform("groovy", "UNIVERSAL")
        .futureValue shouldBe empty
    }

    // A State API blip or an unrecognised platform (400 for FREE_BSD/SUN_OS)
    // must degrade to an empty listing, never surface as a 500 to the CLI.
    forAll(Seq(400, 500, 503)) { status =>
      s"degrade a $status listing read to an empty Seq" in {
        stubListing("groovy", "FREE_BSD")(aResponse().withStatus(status))

        stateApi
          .findVisibleVersionsByCandidateAndPlatform("groovy", "FREE_BSD")
          .futureValue shouldBe empty
      }
    }

    "degrade a timeout to an empty Seq" in {
      // The request timeout is 1500ms (RequestBuilder); a longer delay forces a
      // transport-level failure the client degrades rather than propagates.
      stubListing("groovy", "UNIVERSAL")(
        aResponse().withStatus(200).withBody("[]").withFixedDelay(3000)
      )

      stateApi
        .findVisibleVersionsByCandidateAndPlatform("groovy", "UNIVERSAL")
        .futureValue shouldBe empty
    }

    // Contract drift is not "no versions": a 200 whose body is not Version[]
    // must still fail so the divergence is visible, not silently swallowed.
    "fail on a malformed 200 body" in {
      stubListing("groovy", "UNIVERSAL")(
        aResponse().withStatus(200).withBody("""{"unexpected":"shape"}""")
      )

      stateApi
        .findVisibleVersionsByCandidateAndPlatform("groovy", "UNIVERSAL")
        .failed
        .futureValue shouldBe a[RuntimeException]
    }
  }
}
