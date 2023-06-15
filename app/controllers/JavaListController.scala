package controllers

import io.sdkman.repos.Version

import javax.inject.Inject
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{JavaVersionRendering, VersionItemListBuilder}
import repos.{CandidatesRepository, VersionsRepository}
import utils.{Platform, VersionListProperties}

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

class JavaListController @Inject() (
    versionsRepo: VersionsRepository,
    candidatesRepo: CandidatesRepository,
    cc: ControllerComponents
) extends AbstractController(cc)
    with VersionItemOrdering
    with VersionItemListBuilder
    with VersionListProperties
    with JavaVersionRendering {

  val Candidate = "java"

  def list(platformId: String, current: Option[String], installed: String): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      val platform = Platform(platformId)

      candidatesRepo.findCandidate(Candidate).flatMap { candidate =>
        versionsRepo
          .findAllVisibleVersionsByCandidatePlatform(Candidate, platform.distribution)
          .map { versions =>
            val allLocalVersions: Seq[String] = installed.split(",")

            val localInstalledVersions = findAllNotEndingWith(allLocalVersions, vendors.keySet)

            val vendorInstalledVersions = allLocalVersions.diff(localInstalledVersions)

            val vendorsToItems = versions.groupBy(vendorKey).map { case (ven, vs) =>
              toVendorItems(ven, vs, vendorInstalledVersions.filter(_.endsWith(s"-$ven")), current)
            }

            val sortedVendorToItems = sortItems(vendorsToItems)

            val combinedItems =
              localInstalledVersions.headOption.filter(_.trim.nonEmpty).fold(sortedVendorToItems) {
                _ =>
                  val localInstalledItems =
                    toVendorItems("none", Seq.empty, localInstalledVersions, current)
                  sortedVendorToItems + localInstalledItems
              }

            val defaultVersion = candidate.flatMap(_.default).getOrElse("17.0.0-tem")

            Ok(views.txt.java_version_list(combinedItems, defaultVersion, platform.name))
          }
      }
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
    vendors(vendor) -> items(
      available(versions),
      installed,
      current,
      Some(vendor)
    ).descendingOrder.map(_.show)

  private val vendors = Map(
    "adpt"       -> "AdoptOpenJDK",
    "albba"      -> "Dragonwell",
    "amzn"       -> "Corretto",
    "gln"        -> "Gluon",
    "graalvm-ce" -> "GraalVM Community",
    "graalvm"    -> "Oracle GraalVM",
    "grl"        -> "GraalVM",
    "librca"     -> "Liberica",
    "nik"        -> "Liberica NIK",
    "none"       -> "Unclassified",
    "open"       -> "Java.net",
    "oracle"     -> "Oracle",
    "mandrel"    -> "Mandrel",
    "ms"         -> "Microsoft",
    "sapmchn"    -> "SapMachine",
    "sem"        -> "Semeru",
    "tem"        -> "Temurin",
    "trava"      -> "Trava",
    "zulu"       -> "Zulu",
    "zulufx"     -> "ZuluFX"
  ).mapValues(_.padTo(14, ' '))

  private def sortItems(versionsToItems: Map[String, Seq[String]]): ListMap[String, Seq[String]] =
    ListMap(versionsToItems.toSeq.sortBy(_._1): _*)

}
