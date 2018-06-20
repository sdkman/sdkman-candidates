package ordering

import io.sdkman.repos.Version
import org.scalatest.{Matchers, WordSpec}

import scala.util.Sorting

class SimpleVersionOrderingSpec extends WordSpec with Matchers {
  "Version ordering" should {
    "accurately sort versions by version string" in new RegexStringComparison {

      implicit object OrderedVersion extends Ordering[Version] with RegexStringComparison {
        def compare(v1: Version, v2: Version): Int = compareRegexGroups(v1.version, v2.version)
      }

      val v1 = Version("scala", "2.12.7", "UNIVERSAL", "http://someurl.com")
      val v2 = Version("scala", "2.11.7", "UNIVERSAL", "http://someurl.com")
      val v3 = Version("scala", "2.12.1", "UNIVERSAL", "http://someurl.com")
      val v4 = Version("scala", "2.11.8", "UNIVERSAL", "http://someurl.com")
      val v5 = Version("scala", "2.12.6", "UNIVERSAL", "http://someurl.com")

      val versions = Array(v1, v2, v3, v4, v5)
      Sorting.quickSort(versions)

      versions shouldBe Array(v2, v4, v3, v5, v1)
    }
  }
}
