package steps

import domain.Version
import scalaj.http._

// WARNING: here be heinous mutation sins...
object World {
  val host = "http://localhost:9000"

  var response: HttpResponse[String] = _

  var installedVersions: List[String] = List.empty[String]

  var currentVersion: String = ""

  var candidate: String = ""

  var remoteVersions: List[Version] = List.empty[Version]
}
