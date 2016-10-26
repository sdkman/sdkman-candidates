package steps.support

import scalaj.http._

trait World {
  val host = "http://localhost:9000"

  var response: HttpResponse[String] = null
}
