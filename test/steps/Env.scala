package steps

import cucumber.api.scala.ScalaDsl
import play.api.Logging
import play.test.Helpers.testServer
import support.Mongo

class Env extends ScalaDsl with Logging {
  val app = testServer(9000)
  app.start()

  Before { _ =>
    Mongo.dropAllCollections()
    Mongo.insertAliveOk()
    World.currentVersion = ""
    World.installedVersions = List.empty
  }

  sys.addShutdownHook {
    logger.info("Shutting down test server...")
    app.stop()
  }
}
