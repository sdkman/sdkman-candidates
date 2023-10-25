package ordering

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rendering.VersionItem

class JavaVersionItemOrderingSpec extends AnyWordSpec with Matchers {
  "ordering of VersionItems" when {

    "of same vendor" should {

      "accurately sort versions ascending" in new RegexStringComparison with JavaVersionItemOrdering {
        val v1 = versionItem("17.0.9-tem")
        val v2 = versionItem("17.0.7-tem")
        val v3 = versionItem("8.0.382-tem")
        val v4 = versionItem("17.0.8.1-tem")
        val v5 = versionItem("21.0.1-tem")
        val v6 = versionItem("17.0.8-tem")
        val v7 = versionItem("8.0.372-tem")
        val v8 = versionItem("20.0.2-tem")
        val v9 = versionItem("21-tem")

        val versions = List(v1, v2, v3, v4, v5, v6, v7, v8, v9)

        versions.ascendingOrder shouldBe List(v7, v3, v2, v6, v4, v1, v8, v9, v5)
      }

      "accurately sort versions descending" in new RegexStringComparison with JavaVersionItemOrdering {
        val v1 = versionItem("17.0.9-tem")
        val v2 = versionItem("17.0.7-tem")
        val v3 = versionItem("8.0.382-tem")
        val v4 = versionItem("17.0.8.1-tem")
        val v5 = versionItem("21.0.1-tem")
        val v6 = versionItem("17.0.8-tem")
        val v7 = versionItem("8.0.372-tem")
        val v8 = versionItem("20.0.2-tem")
        val v9 = versionItem("21-tem")

        val versions = List(v1, v2, v3, v4, v5, v6, v7, v8, v9)

        versions.descendingOrder shouldBe List(v5, v9, v8, v1, v4, v6, v2, v3,  v7)
      }
    }

    "of different vendors" should {

      "accurately sort by vendors" in new RegexStringComparison with JavaVersionItemOrdering {
        val v1 = versionItem("21-zulu", Some("zulu"))
        val v2 = versionItem("21-open", Some("open"))
        val v3 = versionItem("21", None)
        val v4 = versionItem("21-oracle", Some("oracle"))
        val v5 = versionItem("21-tem", Some("tem"))
        val v6 = versionItem("21-ms", Some("ms"))

        val versions = List(v1, v2, v3, v4, v5, v6)

        versions.ascendingOrder shouldBe List(v6, v3, v2, v4, v5, v1)
      }

      "accurately sort by vendors and version within vendor" in new RegexStringComparison with JavaVersionItemOrdering {
        val v1 = versionItem("17.0.9-tem", Some("tem"))
        val v2 = versionItem("8.0.382-tem", Some("tem"))
        val v3 = versionItem("17.0.8.1-tem", Some("tem"))
        val v4 = versionItem("21-zulu", Some("zulu"))
        val v5 = versionItem("21.0.1-tem", Some("tem"))
        val v6 = versionItem("17.0.8-tem", Some("tem"))
        val v7 = versionItem("21-oracle", Some("oracle"))
        val v8 = versionItem("20.0.2-tem", Some("tem"))
        val v9 = versionItem("21-tem", Some("tem"))

        val versions = List(v1, v2, v3, v4, v5, v6, v7, v8, v9)

        versions.ascendingOrder shouldBe List(v7, v2, v6, v3, v1, v8, v9, v5, v4)
      }
    }
  }

  private def versionItem(version: String, vendor: Option[String] = None): VersionItem = VersionItem(version, false, false, false, vendor)
}
