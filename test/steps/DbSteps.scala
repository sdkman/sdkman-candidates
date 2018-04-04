package steps

import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import io.sdkman.repos.{Candidate, Version}
import org.scalatest.Matchers
import support.Mongo

import scala.collection.JavaConverters._

class DbSteps extends ScalaDsl with EN with Matchers with World {

  Before { s =>
    Mongo.dropAllCollections()
    Mongo.insertAliveOk()
  }

  And("""^a "([^"]*)" Candidate of Version "([^"]*)" for platform "([^"]*)" at "([^"]*)"$""") { (candidate: String, version: String, platform: String, url: String) =>
    Mongo.insertVersion(Version(candidate, version, platform, url))
  }

  And("""^the Candidates$""") { (candidatesTable: DataTable) =>
    Mongo.insertCandidates(candidatesTable.asList(classOf[Candidate]).asScala)
  }

  And("""^the Candidate$""") { (candidatesTable: DataTable) =>
    Mongo.insertCandidates(candidatesTable.asList(classOf[Candidate]).asScala)
  }

  And("""^the Versions""") { (versionsTable: DataTable) =>
    Mongo.insertVersions(versionsTable.asList(classOf[Version]).asScala)
  }
}
