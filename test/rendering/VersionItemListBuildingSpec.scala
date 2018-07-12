package rendering

import ordering.VersionItemOrdering
import org.scalatest.{Matchers, WordSpec}
import utils.VersionListProperties

class VersionItemListBuildingSpec extends WordSpec with Matchers {
  "VersionItemListBuilding" should {

    val availableVersions = Seq("2.12.5", "2.12.6")

    val installedVersions = List("2.12.5", "2.12.6")

    "build a list of available versions" in new UnderTest {

      items(availableVersions, List.empty, None) shouldBe
        List(
          VersionItem("2.12.5"),
          VersionItem("2.12.6"))
    }

    "build a list of available installed versions" in new UnderTest {

      items(availableVersions, installedVersions, None) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true))
    }

    "build a list of unavailable installed versions" in new UnderTest {

      items(List.empty, installedVersions, None) shouldBe
        List(
          VersionItem("2.12.5", local = true),
          VersionItem("2.12.6", local = true))
    }

    "build a list of available installed versions with a current version set" in new UnderTest {
      val current = Some("2.12.6")
      items(availableVersions, installedVersions, current) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true, current = true))
    }
  }

  private class UnderTest extends VersionItemListBuilding with VersionItemOrdering with VersionListProperties
}
