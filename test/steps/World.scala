package steps

import scalaj.http._

object World {
  val host = "http://localhost:9000"

  var response: HttpResponse[String] = _

  var installedVersions: List[String] = List.empty[String]

  var currentVersion: String = ""
}
