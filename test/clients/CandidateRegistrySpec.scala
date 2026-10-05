package clients

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.config.ConfigFactory
import domain.Candidate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import utils.JsonConverters

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidateRegistrySpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with ScalaFutures
    with JsonConverters {

  // Distinct from the Cucumber (8080) and StateApiImplSpec (8089) servers so
  // all three can coexist in one sbt JVM.
  private val wireMockPort                        = 8090
  private val wireMockServer                      = new WireMockServer(options().port(wireMockPort))
  private implicit val actorSystem: ActorSystem   = ActorSystem("CandidateRegistrySpec")
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

  private val registry =
    new CandidateRegistry(new StateApiImpl(new RequestBuilder(config, wsClient)))

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(50, Millis))

  override def beforeAll(): Unit = wireMockServer.start()

  override def beforeEach(): Unit = {
    wireMockServer.resetAll()
    registry.clear()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    wsClient.close()
    actorSystem.terminate()
  }

  private val goodSet = List(
    Candidate("java", "Java", "The Java platform.", "https://java.com", None),
    Candidate("scala", "Scala", "The Scala language.", "https://scala-lang.org", Some("3.3.1"))
  )

  private def stubCandidates(response: ResponseDefinitionBuilder): Unit =
    wireMockServer.stubFor(get(urlPathEqualTo("/candidates")).willReturn(response))

  private def warmWithGoodSet(): Unit = {
    stubCandidates(aResponse().withStatus(200).withBody(Json.toJson(goodSet).toString))
    registry.refresh().futureValue
    registry.current shouldBe Some(goodSet)
  }

  "refresh" should {

    "load the candidate set from a 200 response" in {
      warmWithGoodSet()
    }

    // An empty registry is never legitimate; serving it would make every
    // `sdk install` reject its candidate, so the previous set must survive.
    "retain the previous set after a 200 empty array" in {
      warmWithGoodSet()
      stubCandidates(aResponse().withStatus(200).withBody("[]"))

      registry.refresh().futureValue
      registry.current shouldBe Some(goodSet)
    }

    "retain the previous set after a 200 malformed body" in {
      warmWithGoodSet()
      stubCandidates(aResponse().withStatus(200).withBody("""{"unexpected":"shape"}"""))

      registry.refresh().futureValue
      registry.current shouldBe Some(goodSet)
    }

    "retain the previous set after a 503" in {
      warmWithGoodSet()
      stubCandidates(aResponse().withStatus(503))

      registry.refresh().futureValue
      registry.current shouldBe Some(goodSet)
    }

    // Overlapping refreshes could land out of order and leave the older set
    // serving; sdkman-state hit exactly this in its own registry holder.
    "issue a single request for two concurrent refreshes" in {
      stubCandidates(
        aResponse()
          .withStatus(200)
          .withBody(Json.toJson(goodSet).toString)
          .withFixedDelay(500)
      )

      Future.sequence(Seq(registry.refresh(), registry.refresh())).futureValue
      registry.current shouldBe Some(goodSet)
      wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/candidates")))
    }
  }

  "clear" should {

    "empty the registry" in {
      warmWithGoodSet()
      registry.clear()
      registry.current shouldBe None
    }
  }
}
