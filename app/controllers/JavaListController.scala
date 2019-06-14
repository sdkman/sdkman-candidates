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

  val vendors = Map(
    "adpt" -> "AdoptOpenJDK",
    "amzn" -> "Amazon",
    "grl" -> "GraalVM",
    "librca" -> "BellSoft",
    "none" -> "Name invalid",
    "open" -> "java.net",
    "sapmchn" -> "SAP",
    "zulu" -> "Azul Zulu",
    "zulufx" -> "Azul ZuluFX"
  ).mapValues(_.padTo(14, ' '))

  def list(uname: String, current: Option[String], installed: Option[String]) =
    Action.async(parse.anyContent) { _ =>

      val platform = Platform(uname).getOrElse(Platform.Universal).identifier

      versionRepo.findAllVersionsByCandidatePlatform("java", platform).map { versions =>

        val vendorsToItems = versions
          .groupBy(vendorKey)
          .map { case (ven, vs) => toVendorItems(ven, vs, installed, current) }

        Ok(views.txt.java_version_list(sortItems(vendorsToItems)))
      }
    }

  private def vendorKey(version: Version): String = version.vendor.getOrElse("none")

  import cats.syntax.show._

  private def toVendorItems(vendor: String,
                            versions: Seq[Version],
                            installed: Option[String],
                            current: Option[String]): (String, Seq[String]) =
    vendors(vendor) -> items(
      available(versions),
      local(installed).filter(_.endsWith(s"-$vendor")),
      current,
      Some(vendor)
    ).descendingOrder.map(_.show)

  private def sortItems(versionsToItems: Map[String, Seq[String]]): ListMap[String, Seq[String]] =
    ListMap(versionsToItems.toSeq.sortBy(_._1): _*)

}
