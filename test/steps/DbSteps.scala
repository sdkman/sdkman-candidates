package steps

import cucumber.api.scala.{EN, ScalaDsl}
import io.cucumber.datatable.DataTable
import domain.{Candidate, Version}
import org.scalatest.matchers.should.Matchers
import support.{Mongo, StateApiStubs}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

class DbSteps extends ScalaDsl with EN with Matchers {

  implicit class CandidateDataTable(dataTable: DataTable) {

    import scala.collection.JavaConverters._

    def toCandidates: Seq[Candidate] =
      dataTable.asMaps().asScala.map(x => rowToCandidate(x.asScala.toMap)).toSeq

    // Tables leave java's `default` blank: its value is the cached Temurin lts.
    private def rowToCandidate(row: Map[String, String]): Candidate =
      Candidate(
        candidate = row("candidate"),
        name = row("name"),
        description = row("description"),
        websiteUrl = row("websiteUrl"),
        default = Option(row("default")).filter(_.nonEmpty)
      )
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
    StateApiStubs.stubCandidates(candidatesTable.toCandidates)
    reloadCandidateRegistry()
  }

  And("""^the Candidate$""") { candidatesTable: DataTable =>
    StateApiStubs.stubCandidates(candidatesTable.toCandidates)
    reloadCandidateRegistry()
  }

  // The app outlives every scenario, so the registry would otherwise answer
  // from the previous scenario's set. A synchronous reload pins it to this
  // scenario's stub (specs/candidate-registry-read-flip.md, §Test fixtures).
  private def reloadCandidateRegistry(): Unit = {
    World.candidateRegistry.clear()
    Await.result(World.candidateRegistry.refresh(), 10.seconds)
  }

  // No set has ever been obtained: every read waits on a fetch that fails, so
  // the registry stays empty (specs/candidate-registry-read-flip.md, §When no
  // set can be obtained).
  And("""^the candidate registry is cold and the State API fails with status (\d+)$""") {
    status: Int =>
      StateApiStubs.stubCandidatesError(status)
      World.candidateRegistry.clear()
  }

  // A failed refresh over a warm registry must retain the previous set.
  And("""^the candidate registry refresh fails with status (\d+)$""") { status: Int =>
    StateApiStubs.stubCandidatesError(status)
    Await.result(World.candidateRegistry.refresh(), 10.seconds)
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
        s"https://downloads/$candidate/$patchVersion/$candidate-$patchVersion.zip",
        visible = Some(true),
        vendor = None
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
            StateApiStubs.stubVersionsForCandidateAndPlatform(
              candidate = candidate,
              platform = platform,
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
            StateApiStubs.stubVersionsForCandidateAndPlatform(
              candidate = candidate,
              platform = platform,
              versions = candidateVersionsByPlatform.sortBy(_.version)
            )
        }
    }
  }

  And("""^no Versions for (.*) of platform (.*) on the remote service$""") {
    (candidate: String, platform: String) =>
      StateApiStubs.stubVersionsForCandidateAndPlatform(
        candidate = candidate,
        platform = platform,
        versions = Seq.empty[Version]
      )
  }

  And("""^the remote service rejects (.*) of platform (.*) with status (\d+)$""") {
    (candidate: String, platform: String, status: Int) =>
      StateApiStubs.stubVersionsErrorForCandidateAndPlatform(
        candidate = candidate,
        platform = platform,
        status = status
      )
  }

  And("""^the remote service returns a malformed body for (.*) of platform (.*)$""") {
    (candidate: String, platform: String) =>
      StateApiStubs.stubMalformedVersionsForCandidateAndPlatform(
        candidate = candidate,
        platform = platform
      )
  }

  And("""^the Version on the remote service$""") { versionsTable: DataTable =>
    val version = versionsTable.toVersions.head
    StateApiStubs.stubVersionForCandidateAndPlatform(
      candidate = version.candidate,
      version = version.version,
      platform = version.platform,
      url = version.url,
      vendor = version.vendor.filter(_.nonEmpty)
    )
  }

  And("""^no Version for (.*) (.*) (.*) of platform (.*) on the remote service$""") {
    (candidate: String, version: String, vendor: String, platform: String) =>
      StateApiStubs.stubNoVersionForCandidateAndPlatform(
        candidate = candidate,
        version = version,
        platform = platform,
        vendor = Option(vendor).filterNot(_.isBlank)
      )
  }

  And("""^the default Version on the remote service$""") { table: DataTable =>
    table.asLists().asScala.tail.foreach { row =>
      val cells = row.asScala.toList
      StateApiStubs.stubVersionForCandidateAndTag(
        candidate = cells.head,
        tag = cells(1),
        platform = cells(2),
        vendor = Option(cells(3)).filter(_.nonEmpty),
        version = cells(4)
      )
    }
  }

  And("""^no default Version for (.*) tag (.*) of platform (.*) on the remote service$""") {
    (candidate: String, tag: String, platform: String) =>
      StateApiStubs.stubNoVersionForCandidateAndTag(
        candidate = candidate,
        tag = tag,
        platform = platform,
        vendor = None
      )
  }
}
