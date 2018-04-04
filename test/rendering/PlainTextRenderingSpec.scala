package rendering

import io.sdkman.repos.Candidate
import org.scalatest.{Matchers, WordSpec}

class PlainTextRenderingSpec extends WordSpec with Matchers {

  val candidate = Candidate("scala", "Scala", "The Scala Language", "2.12.1", "https://www.scala-lang.org/", "UNIVERSAL")

  val section = new CandidateListSection(candidate) with PlainTextRendering { override val ConsoleWidth = 42}

  "PlainTextSectionRendering" should {

    "render a fixed width header" in {
      val expectedHeader = "Scala (2.12.1) https://www.scala-lang.org/"

      section.header shouldBe expectedHeader
    }

    "render a fixed width footer" in {
      val expectedFooter = "                       $ sdk install scala"

      section.footer shouldBe expectedFooter
    }

    "render a complete section" in {
      val expectedSection =
        """Scala (2.12.1) https://www.scala-lang.org/
          |
          |The Scala Language
          |
          |                       $ sdk install scala""".stripMargin

      section.render() shouldBe expectedSection
    }
  }
}
