package controllers

import clients.StateApiImpl
import domain.{Platform, Version}
import ordering.JavaVersionItemOrdering
import play.api.mvc._
import rendering.{JavaVersionRendering, VersionItemListBuilder}
import repos.CandidatesRepository
import utils.VersionListProperties

import javax.inject.Inject
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

class JavaListController @Inject() (
    stateApi: StateApiImpl,
    candidatesRepo: CandidatesRepository,
    cc: ControllerComponents
) extends AbstractController(cc)
    with JavaVersionItemOrdering
    with VersionItemListBuilder
    with VersionListProperties
    with JavaVersionRendering {

  val Candidate = "java"

  def list(platformId: String, current: Option[String], installed: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      for {
        candidateO <- candidatesRepo.findCandidate(Candidate)
        platform = Platform(platformId)
        versions <- stateApi.findVisibleVersionsByCandidateAndPlatform(
          Candidate,
          platform.name
        )
        allLocalVersions: Seq[String] = installed.split(",")
        localInstalledVersions        = findAllNotEndingWith(allLocalVersions, vendors.keySet)
        vendorInstalledVersions       = allLocalVersions.diff(localInstalledVersions)
        vendorsToItems = versions.groupBy(vendorKey).map { case (ven, vs) =>
          toVendorItems(ven, vs, vendorInstalledVersions.filter(_.endsWith(s"-$ven")), current)
        }
        sortedVendorToItems = sortItems(vendorsToItems)
        combinedItems =
          localInstalledVersions.headOption.filter(_.trim.nonEmpty).fold(sortedVendorToItems) { _ =>
            val localInstalledItems =
              toVendorItems("none", Seq.empty, localInstalledVersions, current)
            sortedVendorToItems + localInstalledItems
          }
        defaultVersion = candidateO.flatMap(_.default).getOrElse("17.0.0-tem")
      } yield Ok(views.txt.java_version_list(combinedItems, defaultVersion, platform.description))
    }

  private[controllers] def findAllNotEndingWith(all: Seq[String], endings: Set[String]) =
    all.filter(name => !endings.exists(ending => name.endsWith(ending)))

  private def vendorKey(version: Version): String = version.vendor.getOrElse("none")

  import cats.syntax.show._

  private def toVendorItems(
      vendor: String,
      versions: Seq[Version],
      installed: Seq[String],
      current: Option[String]
  ): (String, Seq[String]) =
    vendors.getOrElse(vendor, "Unclassified") -> items(
      available(versions),
      installed,
      current,
      Some(vendor)
    ).descendingOrder.map(_.show)

  private val vendors = Map(
    "adpt"    -> "AdoptOpenJDK",
    "albba"   -> "Dragonwell",
    "amzn"    -> "Corretto",
    "bisheng" -> "Huawei",
    "gln"     -> "Gluon",
    "graalce" -> "GraalVM CE",
    "graal"   -> "GraalVM Oracle",
    "jbr"     -> "JetBrains",
    "kona"    -> "Tencent",
    "librca"  -> "Liberica",
    "nik"     -> "Liberica NIK",
    "none"    -> "Unclassified",
    "open"    -> "Java.net",
    "oracle"  -> "Oracle",
    "mandrel" -> "Mandrel",
    "ms"      -> "Microsoft",
    "sapmchn" -> "SapMachine",
    "sem"     -> "Semeru",
    "tem"     -> "Temurin",
    "trava"   -> "Trava",
    "zulu"    -> "Zulu",
    "zulufx"  -> "ZuluFX"
  ).mapValues(_.padTo(14, ' '))

  private def sortItems(versionsToItems: Map[String, Seq[String]]): ListMap[String, Seq[String]] =
    ListMap(versionsToItems.toSeq.sortBy(_._1): _*)

}
