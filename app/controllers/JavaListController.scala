package controllers

import io.sdkman.repos.Version
import javax.inject.Inject
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{JavaVersionRendering, VersionItemListBuilder}
import repos.VersionsRepository
import utils.{Platform, VersionListProperties}

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global

class JavaListController @Inject()(versionRepo: VersionsRepository) extends Controller
  with VersionItemOrdering
  with VersionItemListBuilder
  with VersionListProperties
  with JavaVersionRendering {

  def list(uname: String, current: Option[String], installed: String) =
    Action.async(parse.anyContent) { _ =>

      val platform = Platform(uname).getOrElse(Platform.Universal).identifier

      versionRepo.findAllVersionsByCandidatePlatform("java", platform).map { versions =>

        val allLocalVersions: Seq[String] = installed.split(",")

        val localInstalledVersions = findAllNotEndingWith(allLocalVersions, vendors.keySet)

        val vendorInstalledVersions = allLocalVersions.diff(localInstalledVersions)

        val vendorsToItems = versions
          .groupBy(vendorKey)
          .map { case (ven, vs) =>
            toVendorItems(ven, vs, vendorInstalledVersions.filter(_.endsWith(s"-$ven")), current)
          }

        val combinedItems = localInstalledVersions.headOption.filter(_.trim.nonEmpty).fold(vendorsToItems) { _ =>
          val localInstalledItems = toVendorItems("none", Seq.empty, localInstalledVersions, current)
          vendorsToItems + localInstalledItems
        }

        Ok(views.txt.java_version_list(sortItems(combinedItems)))
      }
    }

  private[controllers] def findAllNotEndingWith(all: Seq[String], endings: Set[String]) =
    all.filter(name => !endings.exists(ending => name.endsWith(ending)))

  private def vendorKey(version: Version): String = version.vendor.getOrElse("none")

  import cats.syntax.show._

  private def toVendorItems(vendor: String,
                            versions: Seq[Version],
                            installed: Seq[String],
                            current: Option[String]): (String, Seq[String]) =
    vendors(vendor) -> items(
      available(versions),
      installed,
      current,
      Some(vendor)
    ).descendingOrder.map(_.show)

  val vendors = Map(
    "adpt" -> "AdoptOpenJDK",
    "albba" -> "Alibaba",
    "amzn" -> "Amazon",
    "grl" -> "GraalVM",
    "librca" -> "BellSoft",
    "none" -> "Unclassified",
    "open" -> "Java.net",
    "mandrel" -> "Mandrel",
    "sapmchn" -> "SAP",
    "zulu" -> "Azul Zulu",
    "zulufx" -> "Azul ZuluFX"
  ).mapValues(_.padTo(14, ' '))

  private def sortItems(versionsToItems: Map[String, Seq[String]]): ListMap[String, Seq[String]] =
    ListMap(versionsToItems.toSeq.sortBy(_._1): _*)

}
