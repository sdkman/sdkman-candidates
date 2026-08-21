package rendering

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JavaVersionItemRenderingSpec extends AnyWordSpec with Matchers {

  import cats.syntax.show._

  "javaItemShow" should {

    "render both markers for a version that is current and installed" in new JavaVersionRendering {
      VersionItem(
        "25.0.4-tem",
        current = true,
        installed = true,
        vendor = Some("tem")
      ).show shouldBe "| > * | 25.0.4             | 25.0.4-tem"
    }

    "render the installed marker alone for an installed version" in new JavaVersionRendering {
      VersionItem(
        "21.0.12+1.1-tem",
        installed = true,
        vendor = Some("tem")
      ).show shouldBe "|   * | 21.0.12+1.1        | 21.0.12+1.1-tem"
    }

    "render a blank use cell for a version that is neither current nor installed" in new JavaVersionRendering {
      VersionItem(
        "17.0.20-tem",
        vendor = Some("tem")
      ).show shouldBe "|     | 17.0.20            | 17.0.20-tem"
    }

    // The qualifier belongs to the version, so these three Liberica rows must differ visibly;
    // stripping at the first hyphen collapsed them all to `21.0.12`.
    "keep a hyphen-introduced qualifier in the version column" in new JavaVersionRendering {
      VersionItem(
        "21.0.12-crac+1.2-librca",
        installed = true,
        vendor = Some("librca")
      ).show shouldBe "|   * | 21.0.12-crac+1.2   | 21.0.12-crac+1.2-librca"
    }

    "render the local marker for a locally installed version" in new JavaVersionRendering {
      VersionItem(
        "8.0.202-zulu",
        local = true,
        vendor = Some("zulu")
      ).show shouldBe "|   + | 8.0.202            | 8.0.202-zulu"
    }

    // An unrecognised shortcode never matches the item's vendor, so the version falls back to
    // dropping the identifier's last hyphen segment.
    "derive the version of an unrecognised shortcode from its last segment" in new JavaVersionRendering {
      VersionItem(
        "11.0.3-local",
        local = true,
        vendor = Some("none")
      ).show shouldBe "|   + | 11.0.3             | 11.0.3-local"
    }

    // Truncation is a width guarantee rather than an expected outcome: no live version is this
    // wide, but an overflowing cell would push the row past the 80 character rule.
    "truncate a version wider than the column to 18 characters" in new JavaVersionRendering {
      VersionItem(
        "21.0.12-crac+1.2.3.4-librca",
        vendor = Some("librca")
      ).show shouldBe "|     | 21.0.12-crac+1.2.3 | 21.0.12-crac+1.2.3.4-librca"
    }

    "render no trailing whitespace, because the identifier is the last column" in new JavaVersionRendering {
      val rows = Seq(
        VersionItem("25.0.4-tem", current = true, installed = true, vendor = Some("tem")),
        VersionItem("21.0.12+1.1-tem", installed = true, vendor = Some("tem")),
        VersionItem("17.0.20-tem", vendor = Some("tem")),
        VersionItem("8.0.202-zulu", local = true, vendor = Some("zulu")),
        VersionItem("11.0.3-local", local = true, vendor = Some("none")),
        VersionItem("21.0.12-crac+1.2-librca", vendor = Some("librca"))
      ).map(_.show)

      every(rows) should not endWith " "
    }
  }
}
