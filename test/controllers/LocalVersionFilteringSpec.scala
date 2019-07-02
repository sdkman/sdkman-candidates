package controllers

import org.scalacheck.{Gen, Prop}
import org.scalatest.prop.{Checkers, GeneratorDrivenPropertyChecks}
import org.scalatest.{Matchers, WordSpec}
import play.api.Logger

import scala.util.Random

class LocalVersionFilteringSpec extends WordSpec with Matchers with GeneratorDrivenPropertyChecks with Checkers {

  "Local version filtering" should {

    val known= Seq("amzn", "open", "zulu")

    val unknown= Seq("local", "oracle", "myxyz")

    val versionGen: Gen[String] = for {
      maj <- Gen.numChar
      min <- Gen.numChar
      patch <- Gen.numChar
    } yield s"$maj.$min.$patch"

    def suffixes(xs: Seq[String]): Gen[String] = for {
      suffixes <- Gen.oneOf(xs)
    } yield suffixes

    def identifiers(suffixGen: Gen[String]): Gen[String] = for {
      version <- versionGen
      suffix <- suffixGen
    } yield s"$version-$suffix"

    val combinedVersions: Gen[(List[String], List[String])] = for {
      knownVersions <- Gen.listOfN(5, identifiers(suffixes(known)))
      unknownVersions <- Gen.listOfN(5, identifiers(suffixes(unknown)))
    } yield (knownVersions, unknownVersions)

    "filter names not ending certain suffixes" in new JavaListController(null) {

      check {
        Prop.forAll(combinedVersions) { case (kvs: Seq[String], ukvs: Seq[String]) =>

          val allVersions = Random.shuffle(kvs ++ ukvs)

          val names = findAllNotEndingWith(allVersions, known.toSet)

          Logger.info(allVersions + " -> " + names + " : ")

          (names diff ukvs).isEmpty
        }
      }
    }
  }
}
