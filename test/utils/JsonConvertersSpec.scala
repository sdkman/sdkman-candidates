package utils

import domain.Version
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

// Pins the two-way translation that lives inside `versionFormat`:
//   wire `distribution` enum  <--reads--   --writes-->  internal `vendor` shortcode
// per specs/vendor-distribution-translation.md. Without this anti-corruption
// layer, enum names leaked into controllers and broke shortcode-keyed logic
// (the Java vendor grouping under "Unclassified", `-tem` installed-version
// correlation, etc.). The tests below are the contract for the boundary.
class JsonConvertersSpec extends AnyWordSpec with Matchers with JsonConverters {

  private val baseFields: JsObject = Json.obj(
    "candidate" -> "java",
    "version"   -> "21.0.5-tem",
    "platform"  -> "LINUX_X64",
    "url"       -> "https://downloads/java/21.0.5-tem/java-21.0.5-tem.tar.gz",
    "visible"   -> true
  )

  private def baseVersion(vendor: Option[String]): Version =
    Version(
      candidate = "java",
      version = "21.0.5-tem",
      platform = "LINUX_X64",
      url = "https://downloads/java/21.0.5-tem/java-21.0.5-tem.tar.gz",
      visible = Some(true),
      vendor = vendor
    )

  "versionFormat reads" should {

    "translate the wire distribution enum name to the internal vendor shortcode" in {
      val json = baseFields + ("distribution" -> JsString("TEMURIN"))
      json.as[Version] shouldBe baseVersion(Some("tem"))
    }

    "translate other canonical enum names to their shortcodes" in {
      val json = baseFields ++ Json.obj(
        "version"      -> "8u111-open",
        "distribution" -> "OPENJDK"
      )
      json.as[Version].vendor shouldBe Some("open")
    }

    "yield no vendor when the distribution field is absent" in {
      baseFields.as[Version] shouldBe baseVersion(None)
    }

    // An unknown enum is not expected per spec, but the boundary must not
    // throw on it — a bad wire value degrades to "no vendor" so the rest of
    // the service (which works on shortcodes) keeps going rather than 500ing.
    "yield no vendor when the distribution value is unmappable" in {
      val json = baseFields + ("distribution" -> JsString("ZX_SPECTRUM"))
      json.as[Version] shouldBe baseVersion(None)
    }
  }

  "versionFormat writes" should {

    "translate the internal vendor shortcode to the wire distribution enum name" in {
      val json = Json.toJson(baseVersion(Some("tem")))
      (json \ "distribution").as[String] shouldBe "TEMURIN"
    }

    "omit the distribution field when there is no vendor" in {
      val json = Json.toJson(baseVersion(None))
      (json \ "distribution").toOption shouldBe None
    }

    // Orphan shortcodes (`adpt`, `albba`, `gln`, `trava`, `zulufx`) belong to
    // defunct distributions the State API does not host. Per the spec's
    // best-effort rule the boundary emits no `distribution` rather than a
    // synthetic value — the same shape as "no vendor" on the wire.
    "omit the distribution field when the vendor shortcode has no wire counterpart" in {
      val json = Json.toJson(baseVersion(Some("adpt")))
      (json \ "distribution").toOption shouldBe None
    }
  }

  "versionFormat round-trip" should {

    "round-trip a Version with a mapped vendor shortcode through the JSON form" in {
      val original = baseVersion(Some("open"))
      Json.toJson(original).as[Version] shouldBe original
    }

    "round-trip a Version with no vendor through the JSON form" in {
      val original = baseVersion(None)
      Json.toJson(original).as[Version] shouldBe original
    }
  }
}
