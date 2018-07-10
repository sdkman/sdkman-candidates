package rendering

import org.scalatest.{Matchers, WordSpec}

class VersionItemListBuildingSpec extends WordSpec with Matchers {
  "VersionItemListBuilding" should {

    val available = List("2.12.5", "2.12.6")

    val installed = List("2.12.5", "2.12.6")

    "build a list of available versions" in new VersionItemListBuilding {

      buildItems(available, List.empty, None) shouldBe
        List(
          VersionItem("2.12.5"),
          VersionItem("2.12.6"))
    }

    "build a list of available installed versions" in new VersionItemListBuilding {

      buildItems(available, installed, None) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true))
    }

    "build a list of unavailable installed versions" in new VersionItemListBuilding {

      buildItems(List.empty, installed, None) shouldBe
        List(
          VersionItem("2.12.5", local = true),
          VersionItem("2.12.6", local = true))
    }

    "build a list of available installed versions with a current version set" in new VersionItemListBuilding {
      val current = Some("2.12.6")
      buildItems(available, installed, current) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true, current = true))
    }
  }
}
