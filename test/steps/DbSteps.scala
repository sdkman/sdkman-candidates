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

  And("""^the (.*) (.*) Versions (.*) thru (.*)$""") {
    (platform: String, candidate: String, startVersion: String, endVersion: String) =>
      val startSegs = startVersion.split("\\.")
      val endSegs   = endVersion.split("\\.")

      withClue("only patch ranges allowed") {
        startSegs.length shouldBe 3
        startSegs.length shouldBe endSegs.length
        startSegs.take(2) shouldBe endSegs.take(2)
      }

      val startPatch = startSegs.last.toInt
      val endPatch   = endSegs.last.toInt

      World.candidate = candidate
      val versions = for {
        patch <- (startPatch to endPatch).toList
        patchVersion = s"${startSegs.take(2).mkString(".")}.$patch"
      } yield Version(
        candidate,
        patchVersion,
        platform,
        s"https://downloads/$candidate/$patchVersion/$candidate-$patchVersion.zip"
      )
      World.remoteVersions = World.remoteVersions ++ versions
  }

  And("""^these Versions are available on the remote service$""") {
    Mongo.insertVersions(World.remoteVersions)

    val visibleVersions = World.remoteVersions.filter(_.visible.getOrElse(true))
    visibleVersions.groupBy(_.candidate).foreach {
      case (candidate: String, candidateVersions: Seq[Version]) =>
        candidateVersions.groupBy(_.platform).foreach {
          case (platform: String, candidateVersionsByPlatform: Seq[Version]) =>
            StateApiStubs.stubVersionsForCandidateAndDistribution(
              candidate = candidate,
              distribution = platform,
              versions = candidateVersionsByPlatform.sortBy(_.version)
            )
        }
    }
  }

  And("""^the Versions$""") { versionsTable: DataTable =>
    val versions = versionsTable.toVersions
    Mongo.insertVersions(versions)

    val visibleVersions = versions.filter(_.visible.getOrElse(true))
    visibleVersions.groupBy(_.candidate).foreach {
      case (candidate: String, candidateVersions: Seq[Version]) =>
        candidateVersions.groupBy(_.platform).foreach {
          case (platform: String, candidateVersionsByPlatform: Seq[Version]) =>
            StateApiStubs.stubVersionsForCandidateAndDistribution(
              candidate = candidate,
              distribution = platform,
              versions = candidateVersionsByPlatform.sortBy(_.version)
            )
        }
    }
  }

  And("""^no Versions for (.*) of platform (.*) on the remote service$""") {
    (candidate: String, platform: String) =>
      StateApiStubs.stubVersionsForCandidateAndDistribution(
        candidate = candidate,
        distribution = platform,
        versions = Seq.empty[Version]
      )
  }

  And("""^the Version on the remote service$""") { versionsTable: DataTable =>
    val version = versionsTable.toVersions.head
    StateApiStubs.stubVersionForCandidateAndDistribution(
      candidate = version.candidate,
      version = version.version,
      distribution = version.platform,
      url = version.url,
      vendor = version.vendor.filter(_.nonEmpty)
    )
  }

  And("""^no Version for (.*) (.*) (.*) of platform (.*) on the remote service$""") {
    (candidate: String, version: String, vendor: String, platform: String) =>
      StateApiStubs.stubNoVersionForCandidateAndDistribution(
        candidate = candidate,
        version = version,
        distribution = platform,
        vendor = Option(vendor).filterNot(_.isBlank)
      )
  }
}
