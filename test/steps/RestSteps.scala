package steps

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import play.api.libs.json.Json

import scalaj.http.Http

class RestSteps extends ScalaDsl with EN with Matchers with Env {

  When("""^a request is made to the (.*) endpoint$""") { (endpoint: String) =>
    response = Http(s"$host$endpoint").asString
  }

  Then("""^an (\d+) status is received$""") { (status: Int) =>
    response.code shouldBe status
  }

  Then("""^the payload's "(.*)" is "(.*)"$""") { (key: String, value: String) =>
    val json = Json.parse(response.body)
    val status = (json \ key).as[String]
    status shouldBe value
  }
}
