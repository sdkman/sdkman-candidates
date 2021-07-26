package rendering

import ordering.VersionItemOrdering
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.VersionListProperties

class VersionItemListBuilderSpec extends AnyWordSpec with Matchers {
  "VersionItemListBuilder" should {

    val availableVersions = Seq("2.12.5", "2.12.6")

    val installedVersions = List("2.12.5", "2.12.6")

    "build a list of available version items" in new UnderTest {

      items(availableVersions, installed = List.empty, current = None) shouldBe
        List(
          VersionItem("2.12.5"),
          VersionItem("2.12.6"))
    }

    "build a list of available version items with blank current version" in new UnderTest {
      items(availableVersions, installed = List.empty, current = Some("")) shouldBe
        List(
          VersionItem("2.12.5"),
          VersionItem("2.12.6"))
    }

    "build a list of available installed version items" in new UnderTest {

      items(availableVersions, installedVersions, current = None) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true))
    }

    "build a list of unavailable installed version items" in new UnderTest {

      items(available = List.empty, installedVersions, current = None) shouldBe
        List(
          VersionItem("2.12.5", local = true),
          VersionItem("2.12.6", local = true))
    }

    "build a list of available installed version items with a current version set" in new UnderTest {
      val current = Some("2.12.6")
      items(availableVersions, installedVersions, current) shouldBe
        List(
          VersionItem("2.12.5", installed = true),
          VersionItem("2.12.6", installed = true, current = true))
    }
  }

  private class UnderTest extends VersionItemListBuilder with VersionItemOrdering with VersionListProperties
}
