package clients

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.config.ConfigFactory
import domain.{Candidate, Version}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import utils.JsonConverters

import java.time.{Clock, Instant, ZoneId, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class CandidateRegistrySpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with ScalaFutures
    with Eventually
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
         |}
         |candidate-registry.refresh-interval = 5 minutes""".stripMargin
    )
  )

  // A clock the cases advance by hand, so staleness needs no real waiting.
  private class TestClock extends Clock {
    @volatile var now: Instant                 = Instant.parse("2026-10-05T00:00:00Z")
    def advance(by: FiniteDuration): Unit      = now = now.plusMillis(by.toMillis)
    override def instant(): Instant            = now
    override def getZone: ZoneId               = ZoneOffset.UTC
    override def withZone(zone: ZoneId): Clock = this
  }

  private val clock = new TestClock

  private val registry =
    new CandidateRegistry(new StateApiImpl(new RequestBuilder(config, wsClient)), config, clock)

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

  private val javaLts = Version(
    candidate = "java",
    version = "25.0.4",
    platform = "LINUX_X64",
    url = "https://downloads/java/25.0.4-tem/java-25.0.4-tem.tar.gz",
    visible = Some(true),
    vendor = Some("tem")
  )

  private val javaTagPath = urlPathEqualTo("/versions/java/tags/lts")

  private def stubJavaLts(response: ResponseDefinitionBuilder): Unit =
    wireMockServer.stubFor(
      get(javaTagPath)
        .withQueryParam("platform", equalTo("LINUX_X64"))
        .withQueryParam("distribution", equalTo("TEMURIN"))
        .willReturn(response)
    )

  private def warmWithJavaLts(): Unit = {
    stubCandidates(aResponse().withStatus(200).withBody(Json.toJson(goodSet).toString))
    stubJavaLts(aResponse().withStatus(200).withBody(Json.toJson(javaLts).toString))
    registry.refresh().futureValue
    registry.currentJavaDefault shouldBe Some("25.0.4-tem")
  }

  "refresh of the java default" should {

    // The identifier, never the bare version: conflating the two caused two
    // production rollbacks (docs/glossary.md, identifier / version).
    "resolve the Temurin lts tag at LINUX_X64 to its identifier" in {
      warmWithJavaLts()
      wireMockServer.verify(1, getRequestedFor(javaTagPath))
    }

    // A 404 is a failed refresh, not an answer that java has no default.
    "retain the previous value after a 404" in {
      warmWithJavaLts()
      stubJavaLts(aResponse().withStatus(404))

      registry.refresh().futureValue
      registry.currentJavaDefault shouldBe Some("25.0.4-tem")
    }

    "retain the previous value after a response slower than the 1500 ms timeout" in {
      warmWithJavaLts()
      stubJavaLts(
        aResponse()
          .withStatus(200)
          .withBody(Json.toJson(javaLts.copy(version = "25.0.5")).toString)
          .withFixedDelay(2000)
      )

      registry.refresh().futureValue
      registry.currentJavaDefault shouldBe Some("25.0.4-tem")
    }

    "retain the previous value after a malformed body" in {
      warmWithJavaLts()
      stubJavaLts(aResponse().withStatus(200).withBody("""{"unexpected":"shape"}"""))

      registry.refresh().futureValue
      registry.currentJavaDefault shouldBe Some("25.0.4-tem")
    }

    "stay empty when the first refresh gets a 404" in {
      stubCandidates(aResponse().withStatus(200).withBody(Json.toJson(goodSet).toString))
      stubJavaLts(aResponse().withStatus(404))

      registry.refresh().futureValue
      registry.current shouldBe Some(goodSet)
      registry.currentJavaDefault shouldBe None
    }
  }

  "clear" should {

    "empty the registry" in {
      warmWithGoodSet()
      registry.clear()
      registry.current shouldBe None
      registry.currentJavaDefault shouldBe None
    }
  }

  private val candidatesPath = urlPathEqualTo("/candidates")

  "candidates" should {

    "serve a fresh set without a State API request" in {
      warmWithGoodSet()
      wireMockServer.resetRequests()

      registry.candidates().futureValue shouldBe Some(goodSet)
      wireMockServer.verify(0, getRequestedFor(candidatesPath))
    }

    // A request never blocks on a refresh: the stale copy serves while the
    // refresh it triggered is still in flight.
    "serve a stale set at once and refresh it behind the read" in {
      warmWithGoodSet()
      val newSet = goodSet.take(1)
      stubCandidates(
        aResponse()
          .withStatus(200)
          .withBody(Json.toJson(newSet).toString)
          .withFixedDelay(500)
      )
      clock.advance(6.minutes)

      registry.candidates().futureValue shouldBe Some(goodSet)
      eventually(registry.current shouldBe Some(newSet))
      wireMockServer.verify(2, getRequestedFor(candidatesPath))
    }

    // A restart against a healthy State API must not produce errors.
    "wait for the fetch when no set is held" in {
      stubCandidates(
        aResponse()
          .withStatus(200)
          .withBody(Json.toJson(goodSet).toString)
          .withFixedDelay(500)
      )

      registry.candidates().futureValue shouldBe Some(goodSet)
    }

    "yield None when no set is held and the fetch fails" in {
      stubCandidates(aResponse().withStatus(503))

      registry.candidates().futureValue shouldBe None
    }
  }

  "javaDefault" should {

    "wait for the fetch when no set is held" in {
      stubCandidates(aResponse().withStatus(200).withBody(Json.toJson(goodSet).toString))
      stubJavaLts(aResponse().withStatus(200).withBody(Json.toJson(javaLts).toString))

      registry.javaDefault().futureValue shouldBe Some("25.0.4-tem")
    }

    "serve the held value without a State API request while a set is held" in {
      warmWithJavaLts()
      wireMockServer.resetRequests()

      registry.javaDefault().futureValue shouldBe Some("25.0.4-tem")
      wireMockServer.verify(0, getRequestedFor(javaTagPath))
      wireMockServer.verify(0, getRequestedFor(candidatesPath))
    }
  }
}
