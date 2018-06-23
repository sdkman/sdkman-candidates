package ordering

import io.sdkman.repos.Version
import org.scalatest.{Matchers, WordSpec}
import play.api.Logger

import scala.io.Source
import scala.util.{Random, Sorting}

class RandomOrderingSpec extends WordSpec with Matchers {

  "regex string ordering" should {

    val candidates = Source.fromFile("test/resources/candidates.txt").getLines.toList

    val candidateVersions = candidates.map(c => c -> Source.fromFile(s"test/resources/candidates/$c.txt").getLines.toList).toMap

    "accurately order actual versions" in new RegexStringComparison with VersionOrdering {
      candidates.foreach { candidate =>
        val versions = candidateVersions(candidate)
          .map(Version(candidate, _, "platform", "url"))

        val versionArray = Random.shuffle(versions).toArray

        Logger.info(s"Convert $candidate shuffled: ${versionArray.map(_.version).mkString(",")}")
        Sorting.quickSort(versionArray)
        Logger.info(s"to $candidate ordered: ${versionArray.map(_.version).mkString(",")}")

        versionArray shouldBe versions.toArray
      }
    }
  }
}
