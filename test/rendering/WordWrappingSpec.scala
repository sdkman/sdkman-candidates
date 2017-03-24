package rendering

import org.scalacheck.{Gen, Prop}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

class WordWrappingSpec extends WordSpec with GeneratorDrivenPropertyChecks with Matchers {

  "WordWrapping" should {

    val wordGen: Gen[String] = for {
      wordLen <- Gen.choose(1, 10)
      word <- Gen.listOfN(wordLen, Gen.alphaChar)
    } yield word.mkString

    val paragraphGen: Gen[List[String]] = for {
      paraLen <- Gen.choose(50, 10000)
      paragraph <- Gen.listOfN(paraLen, wordGen)
    } yield paragraph


    "wrap text never to exceed max column width" in new WordWrapping {

      override def ConsoleWidth = 80

      forAll(paragraphGen) { paragraph =>
        val lines = wrapText(paragraph.mkString(" "))
        withClue(s"Exceeded max column width: $ConsoleWidth") {
          lines.foldRight(false)((line, prev) => line.length > ConsoleWidth || prev) shouldBe false
        }
      }
    }

    "wrap text occassionally reaching max column width" in new WordWrapping {

      override def ConsoleWidth = 20

      forAll(paragraphGen) { paragraph =>
        val lines = wrapText(paragraph.mkString(" "))
        withClue(s"Never reached max column width: $ConsoleWidth") {
          lines.foldRight(false)((line, prev) => line.length == ConsoleWidth || prev) shouldBe true
        }
      }
    }
  }
}
