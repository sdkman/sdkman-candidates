package rendering

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class VersionItemRenderingSpec extends AnyWordSpec with Matchers {

  import cats.syntax.show._

  "versionItemShow" should {

    "render a standard version padded to 15 chars" in new VersionRendering {
      VersionItem("2.12.6").show shouldBe "     2.12.6         "
    }

    "render an empty version padded to 15 chars" in new VersionRendering {
      VersionItem("").show shouldBe "                    "
    }

    "render an exact length version at 15, padded at 15 chars" in new VersionRendering {
      VersionItem("123456789012345").show shouldBe "     123456789012345"
    }

    "render an excessively long version by truncating at 15, padded at 15 chars" in new VersionRendering {
      VersionItem("12345678901234567890").show shouldBe "     123456789012345"
    }

    "render a local version" in new VersionRendering {
      VersionItem("2.12.6", local = true).show shouldBe "   + 2.12.6         "
    }

    "render an installed version" in new VersionRendering {
      VersionItem("2.12.6", installed = true).show shouldBe "   * 2.12.6         "
    }

    "render a current local version" in new VersionRendering {
      VersionItem("2.12.6", current = true, local = true).show shouldBe " > + 2.12.6         "
    }

    "render a current installed version" in new VersionRendering {
      VersionItem("2.12.6", current = true, installed = true).show shouldBe " > * 2.12.6         "
    }
  }

  "rowShow" should {

    "render a single version on a row" in new VersionRendering {
      val v1 = VersionItem("2.12.6")

      val row = VersionRow(Some(v1), None, None, None).show
      row shouldBe "     2.12.6                                                                     "
    }

    "render two versions on a row" in new VersionRendering {
      val v1 = VersionItem("2.12.5")
      val v2 = VersionItem("2.12.6")

      val row = VersionRow(Some(v1), Some(v2), None, None).show
      row shouldBe "     2.12.5              2.12.6                                                 "
    }

    "render three versions on a row" in new VersionRendering {
      val v1 = VersionItem("2.12.5")
      val v2 = VersionItem("2.12.6")
      val v3 = VersionItem("2.12.7")

      val row = VersionRow(Some(v1), Some(v2), Some(v3), None).show
      row shouldBe "     2.12.5              2.12.6              2.12.7                             "
    }

    "render four versions on a row" in new VersionRendering {
      val v1 = VersionItem("2.12.5")
      val v2 = VersionItem("2.12.6")
      val v3 = VersionItem("2.12.7")
      val v4 = VersionItem("2.12.8")

      val row = VersionRow(Some(v1), Some(v2), Some(v3), Some(v4)).show
      row shouldBe "     2.12.5              2.12.6              2.12.7              2.12.8         "
    }
  }
}
