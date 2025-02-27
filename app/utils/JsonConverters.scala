package utils

import domain.Version

trait JsonConverters {
  import play.api.libs.json._
  implicit val versionReads: Reads[Version] = Json.reads[Version]
}
