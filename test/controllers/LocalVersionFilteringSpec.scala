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

    // A label joins a vendor group on a `-<shortcode>` suffix only, so a hyphenless label
    // that merely ends in the shortcode letters is not a member.
    def hyphenless(suffixGen: Gen[String]): Gen[String] =
      for {
        version <- versionGen
        suffix  <- suffixGen
      } yield s"$version$suffix"

    val combinedVersions: Gen[(List[String], List[String], List[String])] = for {
      knownVersions     <- Gen.listOfN(5, identifiers(suffixes(known)))
      unknownVersions   <- Gen.listOfN(5, identifiers(suffixes(unknown)))
      hyphenlessVersion <- Gen.listOfN(5, hyphenless(suffixes(known)))
    } yield (knownVersions, unknownVersions, hyphenlessVersion)

    "retain every name that does not carry a `-<suffix>` ending" in
      new JavaListController(null, null, null) {

        check {
          Prop.forAll(combinedVersions) {
            case (kvs: Seq[String], ukvs: Seq[String], hvs: Seq[String]) =>
              val allVersions = Random.shuffle(kvs ++ ukvs ++ hvs)

              val names = findAllNotEndingWith(allVersions, known.toSet)

              logger.info(allVersions + " -> " + names + " : ")

              names.sorted == (ukvs ++ hvs).sorted
          }
        }
      }

    // Both labels used to be claimed by the Temurin group on a bare `endsWith("tem")` match
    // and then filtered out of it again by the `-tem` suffix test, so neither ever rendered.
    "retain a bare shortcode label and a hyphenless label ending in one" in
      new JavaListController(null, null, null) {

        findAllNotEndingWith(Seq("system", "tem"), Set("tem")) shouldBe Seq("system", "tem")
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

    // Published versions with an unmapped shortcode, published versions with no vendor and
    // local-only installs all carry the `Unclassified` label. Collecting the pairs into a Map
    // let the last one silently overwrite the others, dropping whole rows from the response.
    "merge groups that share a display label and keep the pair order" in
      new JavaListController(null, null, null) {

        val merged = mergeByLabel(
          Seq(
            "Temurin        " -> Seq("| tem row"),
            UnclassifiedLabel -> Seq("| published row"),
            "Zulu           " -> Seq("| zulu row"),
            UnclassifiedLabel -> Seq("| local row")
          )
        )

        merged.map(_._1) shouldBe Seq("Temurin        ", UnclassifiedLabel, "Zulu           ")
        merged(1)._2 shouldBe Seq("| published row", "| local row")
      }
  }
}
