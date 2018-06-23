package ordering

import io.sdkman.repos.Version
import org.scalatest.{Matchers, WordSpec}

import scala.util.Sorting

class SimpleVersionOrderingSpec extends WordSpec with Matchers {
  "Version ordering" should {
    "accurately sort versions by version string" in new RegexStringComparison with VersionOrdering {

      val v1 = Version("scala", "2.12.7", "UNIVERSAL", "http://someurl.com")
      val v2 = Version("scala", "2.11.7", "UNIVERSAL", "http://someurl.com")
      val v3 = Version("scala", "2.12.1", "UNIVERSAL", "http://someurl.com")
      val v4 = Version("scala", "2.11.8", "UNIVERSAL", "http://someurl.com")
      val v5 = Version("scala", "2.12.6", "UNIVERSAL", "http://someurl.com")

      val versions = Array(v1, v2, v3, v4, v5)
      Sorting.quickSort(versions)

      versions shouldBe Array(v2, v4, v3, v5, v1)
    }

    "accurately sort 10 and 9 version ranges" in new RegexStringComparison with VersionOrdering {
      val v1 = Version("java", "10.0.0-oracle", "UNIVERSAL", "http://someurl.com")
      val v2 = Version("java", "1.0.0-graal", "UNIVERSAL", "http://someurl.com")
      val v3 = Version("java", "9.0.0-zulu", "UNIVERSAL", "http://someurl.com")

      val versions = Array(v1, v2, v3)
      Sorting.quickSort(versions)

      versions shouldBe Array(v2, v3, v1)
    }

    "accurately sort java vendors" in new RegexStringComparison with VersionOrdering {
      val v1 = Version("java", "10.0.1-zulu", "UNIVERSAL", "http://someurl.com")
      val v2 = Version("java", "10.0.1-openjdk", "UNIVERSAL", "http://someurl.com")
      val v3 = Version("java", "10.0.1-oracle", "UNIVERSAL", "http://someurl.com")

      val versions = Array(v1, v2, v3)
      Sorting.quickSort(versions)

      versions shouldBe Array(v2, v3, v1)
    }
  }
}
