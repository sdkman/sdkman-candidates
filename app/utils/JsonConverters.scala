package utils

import io.sdkman.repos.Version

trait JsonConverters {
  import play.api.libs.json._
  implicit val versionReads: Reads[Version] = Json.reads[Version]
}
