package clients

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class VendorDistributionSpec extends AnyWordSpec with Matchers {

  // Every row of the canonical mapping in
  // specs/vendor-distribution-translation.md, asserted in both directions so
  // that future edits to either column trip a test rather than silently
  // diverging from the spec.
  private val canonicalMapping: List[(String, String)] = List(
    "tem"     -> "TEMURIN",
    "amzn"    -> "CORRETTO",
    "zulu"    -> "ZULU",
    "librca"  -> "LIBERICA",
    "nik"     -> "LIBERICA_NIK",
    "oracle"  -> "ORACLE",
    "open"    -> "OPENJDK",
    "graal"   -> "GRAALVM",
    "graalce" -> "GRAALCE",
    "mandrel" -> "MANDREL",
    "ms"      -> "MICROSOFT",
    "sapmchn" -> "SAP_MACHINE",
    "sem"     -> "SEMERU",
    "kona"    -> "KONA",
    "bisheng" -> "BISHENG",
    "jbr"     -> "JETBRAINS"
  )

  // The orphan shortcodes carry historical meaning in version identifiers but
  // are defunct: the State API hosts no versions for them, so the mapping
  // must report them as having no wire counterpart (best-effort per spec).
  private val orphanShortcodes =
    List("adpt", "albba", "gln", "trava", "zulufx")

  "VendorDistribution.toDistribution" should {
    canonicalMapping.foreach { case (shortcode, distribution) =>
      s"map shortcode '$shortcode' to distribution '$distribution'" in {
        VendorDistribution.toDistribution(shortcode) shouldBe Some(distribution)
      }
    }

    orphanShortcodes.foreach { shortcode =>
      s"return None for orphan shortcode '$shortcode'" in {
        VendorDistribution.toDistribution(shortcode) shouldBe None
      }
    }

    "return None for an unknown shortcode" in {
      VendorDistribution.toDistribution("nope") shouldBe None
    }
  }

  "VendorDistribution.toVendor" should {
    canonicalMapping.foreach { case (shortcode, distribution) =>
      s"map distribution '$distribution' to shortcode '$shortcode'" in {
        VendorDistribution.toVendor(distribution) shouldBe Some(shortcode)
      }
    }

    "return None for an unknown distribution" in {
      VendorDistribution.toVendor("NOPE") shouldBe None
    }
  }
}
