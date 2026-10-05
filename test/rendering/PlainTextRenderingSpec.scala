package rendering

import domain.Candidate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlainTextRenderingSpec extends AnyWordSpec with Matchers {

  val scala = Candidate(
    "scala",
    "Scala",
    "The Scala Language",
    "https://www.scala-lang.org/",
    Some("2.12.1")
  )
  val sectionWithDefault = new CandidateListSection(scala) with PlainTextRendering {
    override val ConsoleWidth = 42
  }

  val micronaut = Candidate(
    "micronaut",
    "Micronaut",
    "The Micronaut Framework",
    "http://micronaut.io",
    None
  )
  val sectionWithoutDefault = new CandidateListSection(micronaut) with PlainTextRendering {
    override val ConsoleWidth = 44
  }

  "PlainTextSectionRendering" should {

    "render a fixed width header with default" in {
      val expectedHeader = "Scala (2.12.1) https://www.scala-lang.org/"

      sectionWithDefault.header shouldBe expectedHeader
    }

    "render a fixed width header without default" in {
      val expectedHeader = "Micronaut (Coming soon!) http://micronaut.io"

      sectionWithoutDefault.header shouldBe expectedHeader
    }

    "render a fixed width footer" in {
      val expectedFooter = "                       $ sdk install scala"

      sectionWithDefault.footer shouldBe expectedFooter
    }

    "render a complete section" in {
      val expectedSection =
        """Scala (2.12.1) https://www.scala-lang.org/
          |
          |The Scala Language
          |
          |                       $ sdk install scala""".stripMargin

      sectionWithDefault.render() shouldBe expectedSection
    }
  }
}
