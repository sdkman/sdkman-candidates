package steps

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import play.api.libs.json.Json

import scalaj.http.Http

class RestSteps extends ScalaDsl with EN with Matchers with World {

  And("""^a request is made to the (.*) endpoint$""") { (endpoint: String) =>
    response = Http(s"$host$endpoint")
      .timeout(connTimeoutMs = 1000, readTimeoutMs = 10000)
      .asString
  }

  When("""^I attempt validation at endpoint (.*) with "(.*)" Accept Header$""") { (endpoint: String, acceptHeader: String) =>
    response = Http(s"$host$endpoint")
      .timeout(connTimeoutMs = 1000, readTimeoutMs = 10000)
      .header("Accept", acceptHeader)
      .asString
  }

  And("""^a (\d+) status code is received$""") { (status: Int) =>
    response.code shouldBe status
  }

  And("""^the payload has a "(.*)" of "(.*)"$""") { (key: String, value: String) =>
    val json = Json.parse(response.body)
    val status = (json \ key).as[String]
    status shouldBe value
  }

  And("""^the response body is "(.*)"$""") { (body: String) =>
    response.body shouldBe body
  }

  And("""^the response JSON contains valid field as (.*)""") { (valid: Boolean) =>
    val json = Json.parse(response.body)
    (json \ "valid").as[Boolean] shouldBe valid
  }
}
