package steps

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import steps.support.{Mongo, World}

class DbSteps extends ScalaDsl with EN with Matchers with World{

  Before { s =>
    Mongo.insertAliveOk()
  }

  And("""^a "([^"]*)" Candidate of Version "([^"]*)" for platform "([^"]*)" at "([^"]*)"$"""){ (candidate:String, version:String, platform:String, url:String) =>
    Mongo.insertVersion(candidate, version, platform, url)
  }

  After { s =>
    Mongo.dropAllCollections()
  }
}
