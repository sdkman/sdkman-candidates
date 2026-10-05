package rendering

import org.scalacheck.{Gen, Prop}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.{Checkers, ScalaCheckDrivenPropertyChecks}
import play.api.Logging

class WordWrappingSpec
    extends AnyWordSpec
    with ScalaCheckDrivenPropertyChecks
    with Checkers
    with Logging {

  "WordWrapping" should {

    val wordGen: Gen[String] = for {
      wordLen <- Gen.choose(1, 10)
      word    <- Gen.listOfN(wordLen, Gen.alphaChar)
    } yield word.mkString

    val paragraphGen: Gen[List[String]] = for {
      paraLen   <- Gen.choose(50, 10000)
      paragraph <- Gen.listOfN(paraLen, wordGen)
    } yield paragraph

    "wrap text never to exceed max column width" in new WordWrapping {

      override def ConsoleWidth = 80

      check {
        Prop.forAll(paragraphGen) { paragraph =>
          val lines = wrapText(paragraph.mkString(" "))

          logger.info(
            s"never exceed max console width $ConsoleWidth:- words: ${paragraph.size}, lines: ${lines.size}"
          )

          val consoleWidthExceeded =
            lines.foldRight(false)((line, prev) => line.length > ConsoleWidth || prev)

          !consoleWidthExceeded
        }
      }
    }

    // "Occasionally" holds across the sample, not per paragraph: a random paragraph can
    // legitimately wrap without any line landing on exactly ConsoleWidth, which made a
    // per-paragraph property fail intermittently.
    "wrap text occasionally reaching max column width" in new WordWrapping {

      override def ConsoleWidth = 80

      var consoleWidthReached = false

      check {
        Prop.forAll(paragraphGen) { paragraph =>
          val lines = wrapText(paragraph.mkString(" "))

          logger.info(
            s"occasionally reach max console width $ConsoleWidth:- words: ${paragraph.size}, lines: ${lines.size}"
          )

          consoleWidthReached ||= lines.exists(_.length == ConsoleWidth)

          true
        }
      }

      assert(consoleWidthReached)
    }
  }
}
