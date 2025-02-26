package utils

import clients.StateApiVersion
import io.sdkman.repos.Version

trait JsonConverters {
  import play.api.libs.json._
  implicit val versionReads: Reads[Version]                 = Json.reads[Version]
  implicit val stateApiVersionReads: Reads[StateApiVersion] = Json.reads[StateApiVersion]
}
