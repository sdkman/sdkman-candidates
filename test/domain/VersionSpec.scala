package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class VersionSpec extends AnyWordSpec with Matchers {

  private def version(v: String, vendor: Option[String]) =
    Version("java", v, "LINUX_X64", "http://url", Some(true), vendor)

  "Version.identifier" should {

    "append the vendor shortcode when present" in {
      version("25.0.4", Some("tem")).identifier shouldBe "25.0.4-tem"
      version("8.0.272.hs", Some("amzn")).identifier shouldBe "8.0.272.hs-amzn"
    }

    "return the bare version when there is no vendor" in {
      version("5.0.8", None).identifier shouldBe "5.0.8"
      version("6.0.0-alpha-1", None).identifier shouldBe "6.0.0-alpha-1"
    }
  }
}
