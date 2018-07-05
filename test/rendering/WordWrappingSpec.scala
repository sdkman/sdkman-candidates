package rendering

import org.scalacheck.{Gen, Prop}
import org.scalatest.WordSpec
import org.scalatest.prop.{Checkers, GeneratorDrivenPropertyChecks}
import play.api.Logger

class WordWrappingSpec extends WordSpec with GeneratorDrivenPropertyChecks with Checkers {

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

      check {
        Prop.forAll(paragraphGen) { paragraph =>
          val lines = wrapText(paragraph.mkString(" "))

          Logger.info(s"never exceed max console width $ConsoleWidth:- words: ${paragraph.size}, lines: ${lines.size}")

          val consoleWidthExceeded = lines.foldRight(false)((line, prev) => line.length > ConsoleWidth || prev)

          !consoleWidthExceeded
        }
      }
    }

    "wrap text occasionally reaching max column width" in new WordWrapping {

      override def ConsoleWidth = 20

      check {
        Prop.forAll(paragraphGen) { paragraph =>
          val lines = wrapText(paragraph.mkString(" "))

          Logger.info(s"occasionally reach max console width $ConsoleWidth:- words: ${paragraph.size}, lines: ${lines.size}")

          val consoleWidthReached = lines.foldRight(false)((line, prev) => line.length == ConsoleWidth || prev)

          consoleWidthReached
        }
      }
    }
  }
}
