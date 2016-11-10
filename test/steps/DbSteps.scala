package steps

import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import repos.Candidate
import support.Mongo

import scala.collection.JavaConversions._

class DbSteps extends ScalaDsl with EN with Matchers with World {

  Before { s =>
    Mongo.dropAllCollections()
    Mongo.insertAliveOk()
  }

  And("""^a "([^"]*)" Candidate of Version "([^"]*)" for platform "([^"]*)" at "([^"]*)"$""") { (candidate: String, version: String, platform: String, url: String) =>
    Mongo.insertVersion(candidate, version, platform, url)
  }

  And("""^the Candidates$""") { (candidatesTable: DataTable) =>
    Mongo.insertCandidates(candidatesTable.asList(classOf[Candidate]))
  }
}
