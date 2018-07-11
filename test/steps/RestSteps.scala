package steps

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import play.api.libs.json.Json
import scalaj.http.Http
import steps.World._

import scala.annotation.tailrec

class RestSteps extends ScalaDsl with EN with Matchers {

  And("""^the scala Version (.*) is set as current$""") { version: String =>
    currentVersion = version
  }

  And("""^the scala Version (.*) is installed$""") { version: String =>
    installedVersions = List(version)
  }

  And("""^the scala Versions (.*) are installed$""") { versions: String =>
    installedVersions = versions.split(",").toList
  }

  And("""^a request is made to (.*)""") { endpoint: String =>
    val queryParams = Map("current" -> currentVersion, "installed" -> installedVersions.mkString(",")).filter { case (k: String, v: String) => v != "" }
    response = Http(s"$host$endpoint")
      .params(queryParams)
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
      .asString
  }

  When("""^I attempt validation at endpoint (.*)$""") { endpoint: String =>
    response = Http(s"$host$endpoint")
      .timeout(connTimeoutMs = 10000, readTimeoutMs = 10000)
      .asString
  }

  And("""^a (\d+) status code is received$""") { status: Int =>
    response.code shouldBe status
  }

  And("""^the payload has a "(.*)" of "(.*)"$""") { (key: String, value: String) =>
    val json = Json.parse(response.body)
    val status = (json \ key).as[String]
    status shouldBe value
  }

  And("""^the response body is "(.*)"$""") { body: String =>
    response.body shouldBe body
  }

  And("""^the response body is$""") { body: String =>
    strip(response.body) shouldBe body.stripMargin
  }

  And("""^the rendered text is:""") { expectedBody: String =>
    response.body shouldBe expectedBody.stripMargin
  }

  private def strip(s: String): String = s.split("\\n").map(trimEnd).mkString("\n")

  @tailrec
  private def trimEnd(s: String): String = s.takeRight(1) match {
    case " " => trimEnd(s.dropRight(1))
    case _ => s
  }
}
