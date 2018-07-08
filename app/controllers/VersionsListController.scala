package controllers

import javax.inject.Inject
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{VersionItem, VersionRendering, VersionRow}
import repos.VersionsRepository
import utils.Platform

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsListController @Inject()(versionsRepo: VersionsRepository) extends Controller with VersionItemOrdering with VersionRendering {

  val MaxVersions = 60

  val ColumnLength = 15

  def list(candidate: String, platform: String, current: Option[String], installed: List[String]) =
    Action.async(parse.anyContent) { request =>

      //TODO handle platform specific candidates
      versionsRepo.findAllVersionsByCandidatePlatform(candidate, Platform.Universal.identifier).map { versions =>

        val padded = versions.map(v => VersionItem(v.version)).descendingOrder.map(Some(_)).padTo(MaxVersions, None)

        import cats.syntax.show._

        val rows: Seq[String] = for {
          i <- 0 until ColumnLength
          col1 = padded(i + 0 * ColumnLength)
          col2 = padded(i + 1 * ColumnLength)
          col3 = padded(i + 2 * ColumnLength)
          col4 = padded(i + 3 * ColumnLength)

        } yield VersionRow(col1, col2, col3, col4).show

        Ok(views.txt.version_list(candidate.capitalize, rows))
      }
    }
}