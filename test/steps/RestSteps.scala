package steps

import com.github.tomakehurst.wiremock.client.WireMock.{
  anyRequestedFor,
  anyUrl,
  equalTo,
  getRequestedFor,
  urlPathMatching,
  verify
}
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.AppendedClues
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import scalaj.http.Http
import steps.World._

import scala.annotation.tailrec

class RestSteps extends ScalaDsl with EN with Matchers with AppendedClues {

  And("""^the scala Version (.*) is set as current$""") { version: String =>
    currentVersion = version
  }

  And("""^the current Version is (.*)$""") { version: String =>
    currentVersion = version
  }

  And("""^the scala Version (.*) is installed$""") { version: String =>
    installedVersions = List(version)
  }

  And("""^the scala Versions (.*) are installed$""") { versions: String =>
    installedVersions = versions.split(",").toList
  }

  And("""^the installed Versions (.*)$""") { versions: String =>
    installedVersions = versions.split(",").toList
  }

  And("""^a request is made to (.*)""") { endpoint: String =>
    val queryParams =
      Map("current" -> currentVersion, "installed" -> installedVersions.mkString(","))
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
    val json   = Json.parse(response.body)
    val status = (json \ key).as[String]
    status shouldBe value
  }

  And("""^the response body is "(.*)"$""") { body: String =>
    response.body shouldBe body
  }

  // Request-journal assertions against the shared WireMock State API stub (port
  // 8080, journal reset per scenario in Env.Before). These pin call counts the
  // response-only assertions cannot see: a regression giving `/default/java` a
  // fallback, or hitting the State API before the unknown-candidate check, still
  // returns the same status/body but issues a different number of requests.
  And("""^the State API received no requests$""") { () =>
    verify(0, anyRequestedFor(anyUrl()))
  }

  And("""^the State API received exactly (\d+) tag lookups? for (\S+)$""") {
    (count: Int, candidate: String) =>
      verify(count, getRequestedFor(urlPathMatching(s"/versions/$candidate/tags/.*")))
  }

  And(
    """^the State API received exactly (\d+) tag lookups? for (\S+) at platform (\S+) with distribution (\S+)$"""
  ) { (count: Int, candidate: String, platform: String, distribution: String) =>
    verify(
      count,
      getRequestedFor(urlPathMatching(s"/versions/$candidate/tags/.*"))
        .withQueryParam("platform", equalTo(platform))
        .withQueryParam("distribution", equalTo(distribution))
    )
  }

  And("""^the response body is$""") { body: String =>
    strip(response.body) shouldBe body.stripMargin withClue
      s"""|
          |The response was:
          |${response.body}
          |But we required:
          $body
          |""".stripMargin
  }

  // Raw-body width assertion. The `the response body is` step right-trims each
  // line before comparing, so it cannot see padding that pushes a row past the
  // 80-column rule of specs/java-list-view-layout.md. This step reads
  // `response.body` unmodified and is the only end-to-end cover for that rule.
  And("""^every response line is at most 80 characters with no trailing whitespace$""") { () =>
    // A tab or a carriage return is as invisible in a diff as a trailing space,
    // and just as capable of pushing a rendered row past the terminal rule.
    val trailingWhitespace = """.*[ \t\r]""".r
    val lines              = response.body.split("\n", -1).toSeq
    val tooWide            = lines.filter(_.length > 80)
    val trailing           = lines.filter(trailingWhitespace.pattern.matcher(_).matches)

    tooWide shouldBe empty withClue
      s"""|
          |These lines exceed 80 characters:
          |${tooWide.map(l => s"${l.length}: $l").mkString("\n")}
          |""".stripMargin

    trailing shouldBe empty withClue
      s"""|
          |These lines carry trailing whitespace:
          |${trailing.map(l => s"[$l]").mkString("\n")}
          |""".stripMargin
  }

  And("""^the rendered text is:""") { expectedBody: String =>
    response.body shouldBe expectedBody.stripMargin
  }

  private def strip(s: String): String = s.split("\\n").map(trimEnd).mkString("\n")

  @tailrec
  private def trimEnd(s: String): String = s.takeRight(1) match {
    case " " => trimEnd(s.dropRight(1))
    case _   => s
  }
}
