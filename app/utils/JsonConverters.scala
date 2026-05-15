package utils

import domain.Version
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait JsonConverters {

  // The State API names the vendor concept `distribution` on the wire; the
  // Candidates Service keeps `vendor` as the internal identifier (locked
  // decision in specs/state-api-default-version.md). Translation happens here.
  implicit val versionFormat: Format[Version] = (
    (JsPath \ "candidate").format[String] and
      (JsPath \ "version").format[String] and
      (JsPath \ "platform").format[String] and
      (JsPath \ "url").format[String] and
      (JsPath \ "visible").formatNullable[Boolean] and
      (JsPath \ "distribution").formatNullable[String]
  )(Version.apply, unlift(Version.unapply))
}
