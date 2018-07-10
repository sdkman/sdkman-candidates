package steps

import cucumber.api.DataTable
import cucumber.api.scala.{EN, ScalaDsl}
import gherkin.formatter.model.DataTableRow
import io.sdkman.repos.{Candidate, Version}
import org.scalatest.Matchers
import support.Mongo

import scala.collection.JavaConverters._

class DbSteps extends ScalaDsl with EN with Matchers {

  Before { s =>
    Mongo.dropAllCollections()
    Mongo.insertAliveOk()
    World.currentVersion = ""
    World.installedVersions = List.empty
  }

  And("""^a "([^"]*)" Candidate of Version "([^"]*)" for platform "([^"]*)" at "([^"]*)"$""") { (candidate: String, version: String, platform: String, url: String) =>
    Mongo.insertVersion(Version(candidate, version, platform, url))
  }

  implicit class CandidateDataTable(dataTable: DataTable) {

    import scala.collection.JavaConverters._

    def toCandidates: Seq[Candidate] = dataTable.getGherkinRows.asScala.tail.map(rowToCandidate)

    private def rowToCandidate(row: DataTableRow): Candidate = {
      val cells = row.getCells.asScala
      Candidate(candidate = cells.head,
        name = cells(1),
        description = cells(2),
        default = if (cells(3) == "") None else Some(cells(3)),
        websiteUrl = cells(4),
        distribution = cells(5))
    }
  }

  And("""^the Candidates$""") { candidatesTable: DataTable =>
    Mongo.insertCandidates(candidatesTable.toCandidates)
  }

  And("""^the Candidate$""") { candidatesTable: DataTable =>
    Mongo.insertCandidates(candidatesTable.toCandidates)
  }

  And("""the (.*) Versions (.*) thru (.*)""") { (candidate: String, startVersion: String, endVersion: String) =>
    val startSegs = startVersion.split("\\.")
    val endSegs = endVersion.split("\\.")

    withClue("only patch ranges allowed") {
      startSegs.length shouldBe 3
      startSegs.length shouldBe endSegs.length
      startSegs.take(2) shouldBe endSegs.take(2)
    }

    val startPatch = startSegs.last.toInt
    val endPatch = endSegs.last.toInt

    for(patch <- startPatch to endPatch) {
      val version = s"${startSegs.take(2).mkString(".")}.$patch"
      Mongo.insertVersion(
        Version(
          candidate,
          version,
          "UNIVERSAL",
          s"https://downloads/$candidate/$version/$candidate-$version.zip"))
    }
  }

  And("""^the Versions""") { versionsTable: DataTable =>
    Mongo.insertVersions(versionsTable.asList(classOf[Version]).asScala)
  }
}
