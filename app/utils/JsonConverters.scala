package utils

import clients.VendorDistribution
import domain.Version
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait JsonConverters {

  // The State API names the vendor concept `distribution` on the wire and uses
  // an enum-name vocabulary; the Candidates Service keeps `vendor` (a
  // shortcode like `tem`, `open`) as the internal identifier everywhere except
  // this wire boundary. Per specs/vendor-distribution-translation.md, both the
  // field name AND the value are translated here: inbound enum names become
  // shortcodes, outbound shortcodes become enum names. An unmappable value
  // surfaces as None rather than throwing — orphan/defunct shortcodes have no
  // wire counterpart, and an unknown inbound enum (not expected per spec)
  // degrades to "no vendor" rather than crashing the response.
  implicit val versionFormat: Format[Version] = (
    (JsPath \ "candidate").format[String] and
      (JsPath \ "version").format[String] and
      (JsPath \ "platform").format[String] and
      (JsPath \ "url").format[String] and
      (JsPath \ "visible").formatNullable[Boolean] and
      (JsPath \ "distribution")
        .formatNullable[String]
        .inmap[Option[String]](
          _.flatMap(VendorDistribution.toVendor),
          _.flatMap(VendorDistribution.toDistribution)
        )
  )(Version.apply, unlift(Version.unapply))
}
