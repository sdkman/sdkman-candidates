package steps

import cucumber.api.scala.{EN, ScalaDsl}
import io.cucumber.datatable.DataTable
import io.sdkman.repos.{Candidate, Version}
import org.scalatest.matchers.should.Matchers
import support.{Mongo, StateApiStubs}

class DbSteps extends ScalaDsl with EN with Matchers {

  implicit class CandidateDataTable(dataTable: DataTable) {

    import scala.collection.JavaConverters._

    def toCandidates: Seq[Candidate] =
      dataTable.asLists().asScala.tail.map(x => rowToCandidate(x.asScala.toList))

    private def rowToCandidate(row: List[String]): Candidate = {
      val cells = row
      Candidate(
        candidate = cells.head,
        name = cells(1),
        description = cells(2),
        default = if (cells(3) == "") None else Some(cells(3)),
        websiteUrl = cells(4),
        distribution = cells(5)
      )
    }
  }

  implicit class VersionDataTable(dataTable: DataTable) {

    import scala.collection.JavaConverters._

    def toVersions: Seq[Version] =
      dataTable.asLists().asScala.tail.map(x => rowToVersion(x.asScala.toList))

    private def rowToVersion(row: List[String]): Version = {
      val cells = row
      Version(
        candidate = cells.head,
        version = cells(1),
        vendor = if (cells(2) == "") None else Some(cells(2)),
        platform = cells(3),
        url = cells(4),
        visible = if (cells.size == 6 && cells(5).nonEmpty) Some(cells(5).toBoolean) else Some(true)
      )
    }
  }

  And("""^the Candidates$""") { candidatesTable: DataTable =>
    Mongo.insertCandidates(candidatesTable.toCandidates)
  }

  And("""^the Candidate$""") { candidatesTable: DataTable =>
    Mongo.insertCandidates(candidatesTable.toCandidates)
  }

  And("""the (.*) Versions (.*) thru (.*)""") {
    (candidate: String, startVersion: String, endVersion: String) =>
      val startSegs = startVersion.split("\\.")
      val endSegs   = endVersion.split("\\.")

      withClue("only patch ranges allowed") {
        startSegs.length shouldBe 3
        startSegs.length shouldBe endSegs.length
        startSegs.take(2) shouldBe endSegs.take(2)
      }

      val startPatch = startSegs.last.toInt
      val endPatch   = endSegs.last.toInt

      for (patch <- startPatch to endPatch) {
        val version = s"${startSegs.take(2).mkString(".")}.$patch"
        Mongo.insertVersion(
          Version(
            candidate,
            version,
            "UNIVERSAL",
            s"https://downloads/$candidate/$version/$candidate-$version.zip"
          )
        )
      }
  }

  And("""^the Versions$""") { versionsTable: DataTable =>
    val versions  = versionsTable.toVersions
    val candidate = versions.headOption.map(_.candidate).getOrElse("none")
    Mongo.insertVersions(versions)
    versions.groupBy(_.platform).map { case (platform, platformVersions) =>
      StateApiStubs.stubVersions(
        candidate = candidate,
        distribution = platform,
        versions = platformVersions.sortBy(_.version)
      )
    }
  }

  And("""^no Versions for (.*)$""") { (candidate: String) =>
    StateApiStubs.stubVersions(
      candidate = candidate,
      distribution = "UNIVERSAL",
      versions = Seq.empty[Version]
    )
  }
}
