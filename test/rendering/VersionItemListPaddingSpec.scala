package rendering

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class VersionItemListPaddingSpec extends AnyWordSpec with Matchers {
  "VersionItemListBuilding" should {
    "pad a list within MinCountThreshold" in new UnderTest {
      pad(versionItems(1), 4).length shouldBe 4
      pad(versionItems(2), 4).length shouldBe 4
      pad(versionItems(3), 4).length shouldBe 4
      pad(versionItems(4), 4).length shouldBe 4
    }

    "pad a list exceeding MinCountThreshold by 1 row" in new UnderTest {
      pad(versionItems(5), 8).length shouldBe 8
      pad(versionItems(6), 8).length shouldBe 8
      pad(versionItems(7), 8).length shouldBe 8
      pad(versionItems(8), 8).length shouldBe 8
    }

    "pad a list exceeding MinCountThreshold by 2 rows" in new UnderTest {
      pad(versionItems(9), 12).length shouldBe 12
      pad(versionItems(10), 12).length shouldBe 12
      pad(versionItems(11), 12).length shouldBe 12
      pad(versionItems(12), 12).length shouldBe 12
    }
  }

  private def versionItems(count: Int) = for (i <- 0 until count) yield VersionItem(s"$i")

  private class UnderTest extends VersionItemListBuilder {
    override val MinCountThreshold: Int = 4
  }

}
