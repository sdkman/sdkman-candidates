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
        versionsByVendor = versions.groupBy(vendorKey)
        // Membership of a vendor group is keyed on the vendors actually published on this
        // platform, not on the whole shortcode map: a label ending in a shortcode nobody
        // published had no group to land in and used to vanish from the response entirely.
        publishedVendors: Set[String] = versionsByVendor.keySet
        // A leading, trailing or doubled comma splits into empty elements. Left in, the first
        // of them failed the local-list guard below and suppressed the whole Unclassified
        // group, so every local install vanished because of a stray comma.
        allLocalVersions: Seq[String] = installed.split(",").filter(_.trim.nonEmpty)
        localInstalledVersions        = findAllNotEndingWith(allLocalVersions, publishedVendors)
        vendorInstalledVersions       = allLocalVersions.diff(localInstalledVersions)
        vendorsToItems = versionsByVendor.toSeq.map { case (ven, vs) =>
          toVendorItems(ven, vs, vendorInstalledVersions.filter(_.endsWith(s"-$ven")), current)
        }
        allVendorItems =
          if (localInstalledVersions.isEmpty) vendorsToItems
          else {
            val localInstalledItems =
              toVendorItems("none", Seq.empty, localInstalledVersions, current)
            vendorsToItems :+ localInstalledItems
          }
        combinedItems  = sortItems(mergeByLabel(allVendorItems))
        defaultVersion = candidateO.flatMap(_.default).getOrElse("17.0.0-tem")
        // The footer line has a fixed 57-character prefix, so an over-long candidate
        // default is the one value left that can push a response line past 80 characters.
        footerDefault = truncate(defaultVersion, DefaultVersionLength)
      } yield Ok(views.txt.java_version_list(combinedItems, footerDefault, platform.description))
    }

  /** The labels that join no vendor group. A label joins a group only on a `-<shortcode>` suffix,
    * so a bare shortcode (`tem`) and a hyphenless label that merely ends in one (`system`) stay
    * local instead of being claimed by, and then filtered out of, the Temurin group.
    */
  private[controllers] def findAllNotEndingWith(all: Seq[String], endings: Set[String]) =
    all.filter(name => !endings.exists(ending => name.endsWith(s"-$ending")))

  private def vendorKey(version: Version): String = version.vendor.getOrElse("none")

  import cats.syntax.show._

  private[controllers] def toVendorItems(
      vendor: String,
      versions: Seq[Version],
      installed: Seq[String],
      current: Option[String]
  ): (String, Seq[String]) =
    vendors.getOrElse(vendor, UnclassifiedLabel) -> items(
      available(versions),
      installed,
      current,
      Some(vendor)
    ).descendingOrder.map(_.show)

  // A shortcode absent from the map still heads a group, so its label carries the same
  // padding as the mapped labels below; an unpadded fallback would pull the row separator
  // three columns left of 17 for that group's first row.
  private[controllers] val UnclassifiedLabel = "Unclassified".padTo(15, ' ')

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
  ).mapValues(_.padTo(15, ' '))

  /** Groups carrying the same display label render as one group, published rows first and
    * local-only rows after, so a group never silently overwrites another with the same key.
    */
  private[controllers] def mergeByLabel(
      vendorsToItems: Seq[(String, Seq[String])]
  ): Seq[(String, Seq[String])] =
    vendorsToItems.foldLeft(Seq.empty[(String, Seq[String])]) { case (merged, (label, items)) =>
      merged.indexWhere(_._1 == label) match {
        case -1    => merged :+ (label -> items)
        case index => merged.updated(index, label -> (merged(index)._2 ++ items))
      }
    }

  // The catch-all group renders after every vendor group, as the layout spec's reference
  // rendering shows; sorting it alphabetically would move it above Zulu.
  private[controllers] def sortItems(
      versionsToItems: Seq[(String, Seq[String])]
  ): ListMap[String, Seq[String]] = {
    val (unclassified, classified) = versionsToItems.partition(_._1 == UnclassifiedLabel)
    ListMap(classified.sortBy(_._1) ++ unclassified: _*)
  }

}
