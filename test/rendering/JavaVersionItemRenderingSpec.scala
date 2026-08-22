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

    "derive the version of an unrecognised shortcode from its last segment" in new JavaVersionRendering {
      VersionItem(
        "11.0.3-local",
        local = true,
        vendor = Some("none")
      ).show shouldBe "|   + | 11.0.3             | 11.0.3-local"
    }

    "keep a qualifier the fallback cannot mistake for a shortcode" in new JavaVersionRendering {
      VersionItem(
        "21.0.12-crac+1.2",
        local = true,
        vendor = Some("none")
      ).show shouldBe "|   + | 21.0.12-crac+1.2   | 21.0.12-crac+1.2"
    }

    "keep an upper-case trailing segment in the version column" in new JavaVersionRendering {
      VersionItem(
        "UPPER-CASE",
        local = true,
        vendor = Some("none")
      ).show shouldBe "|   + | UPPER-CASE         | UPPER-CASE"
    }

    "truncate a version wider than the column to 17 characters plus the marker" in new JavaVersionRendering {
      VersionItem(
        "21.0.12-crac+1.2.3.4-librca",
        vendor = Some("librca")
      ).show shouldBe "|     | 21.0.12-crac+1.2.> | 21.0.12-crac+1.2.3.4-librca"
    }

    "truncate an identifier wider than the column to 34 characters plus the marker" in new JavaVersionRendering {
      VersionItem(
        "21.0.12-crac+1.2.3.4.5.6.7.10-librca",
        vendor = Some("librca")
      ).show shouldBe "|     | 21.0.12-crac+1.2.> | 21.0.12-crac+1.2.3.4.5.6.7.10-libr>"
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
