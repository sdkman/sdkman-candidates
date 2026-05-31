package clients

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.config.ConfigFactory
import domain.Version
import org.scalatest.BeforeAndAfterAll
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
      // `Version.vendor` is the internal shortcode (`tem`); `Json.toJson(expected)`
      // below applies the boundary translation so the wire body still reads
      // `distribution: "TEMURIN"`. The caller-side argument to
      // `findVersionByCandidateAndTag` still carries the enum name here — the
      // outbound translation is item 5 of the plan, not this commit.
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
        .findVersionByCandidateAndTag("java", "lts", "LINUX_X64", Some("TEMURIN"))
        .futureValue shouldBe Some(expected)
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
}
