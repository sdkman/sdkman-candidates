package controllers

import org.scalacheck.{Gen, Prop}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.{Checkers, ScalaCheckDrivenPropertyChecks}
import play.api.Logging

import scala.util.Random

class LocalVersionFilteringSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks
    with Checkers
    with Logging {

  "Local version filtering" should {

    val known = Seq("amzn", "open", "zulu")

    val unknown = Seq("local", "oracle", "myxyz")

    val versionGen: Gen[String] = for {
      maj   <- Gen.numChar
      min   <- Gen.numChar
      patch <- Gen.numChar
    } yield s"$maj.$min.$patch"

    def suffixes(xs: Seq[String]): Gen[String] =
      for {
        suffixes <- Gen.oneOf(xs)
      } yield suffixes

    def identifiers(suffixGen: Gen[String]): Gen[String] =
      for {
        version <- versionGen
        suffix  <- suffixGen
      } yield s"$version-$suffix"

    val combinedVersions: Gen[(List[String], List[String])] = for {
      knownVersions   <- Gen.listOfN(5, identifiers(suffixes(known)))
      unknownVersions <- Gen.listOfN(5, identifiers(suffixes(unknown)))
    } yield (knownVersions, unknownVersions)

    "filter names not ending certain suffixes" in new JavaListController(null, null, null) {

      check {
        Prop.forAll(combinedVersions) { case (kvs: Seq[String], ukvs: Seq[String]) =>
          val allVersions = Random.shuffle(kvs ++ ukvs)

          val names = findAllNotEndingWith(allVersions, known.toSet)

          logger.info(allVersions + " -> " + names + " : ")

          (names diff ukvs).isEmpty
        }
      }
    }
  }

  "Vendor grouping" should {

    // The mapped labels are padded to the vendor column width; an unpadded fallback would
    // shift the first row's `|` separator off column 17 for any unrecognised shortcode.
    "pad the fallback label of an unmapped shortcode to the vendor column width" in
      new JavaListController(null, null, null) {

        val (label, rows) = toVendorItems("myxyz", Seq.empty, Seq("11.0.3-myxyz"), None)

        label shouldBe "Unclassified   "
        label should have length 15
        rows.head should startWith("|")
      }
  }
}
