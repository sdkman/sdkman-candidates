package steps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import cucumber.api.scala.ScalaDsl
import play.api.Logging
import play.test.Helpers.testServer
import play.test.TestServer
import support.Mongo

class Env extends ScalaDsl with Logging {

  val wireMockServer = new WireMockServer(options().port(8080))
  wireMockServer.start()

  val app: TestServer = testServer(9000)
  app.start()

  Before { _ =>
    Mongo.dropAllCollections()
    Mongo.insertAliveOk()
    WireMock.reset()
    World.currentVersion = ""
    World.installedVersions = List.empty
  }

  sys.addShutdownHook {
    logger.info("Shutting down test server...")
    app.stop()
    wireMockServer.stop()
  }
}
