package controllers

import javax.inject.Inject
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{RowCountCalculator, VersionItemListBuilding, VersionRendering, VersionRow}
import repos.VersionsRepository
import utils.{Platform, VersionListProperties}

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsListController @Inject()(versionsRepo: VersionsRepository)
  extends Controller
    with VersionListProperties
    with VersionItemOrdering
    with VersionRendering
    with VersionItemListBuilding
    with RowCountCalculator {

  def list(candidate: String, platform: String, current: Option[String], installed: Option[String]) =
    Action.async(parse.anyContent) { request =>

      //TODO handle platform specific candidates
      versionsRepo.findAllVersionsByCandidatePlatform(candidate, Platform.Universal.identifier).map { versions =>

        import cats.syntax.show._

        val padded = pad(items(available(versions), local(installed), current).descendingOrder)
        val rowCount = asRowCount(versions.length)
        val rows: Seq[String] = for {
          i <- 0 until rowCount
          col1 = padded(i + 0 * rowCount)
          col2 = padded(i + 1 * rowCount)
          col3 = padded(i + 2 * rowCount)
          col4 = padded(i + 3 * rowCount)

        } yield VersionRow(col1, col2, col3, col4).show

        Ok(views.txt.version_list(candidate.capitalize, rows))
      }
    }
}
