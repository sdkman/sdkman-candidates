package controllers

import javax.inject.Inject
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{RowCountCalculator, VersionItemListBuilder, VersionRendering, VersionRow}
import repos.{CandidatesRepository, VersionsRepository}
import utils.{Platform, VersionListProperties}

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsListController @Inject()(versionsRepo: VersionsRepository, candidatesRepo: CandidatesRepository, cc: ControllerComponents)
  extends AbstractController(cc)
    with VersionListProperties
    with VersionItemOrdering
    with VersionRendering
    with VersionItemListBuilder
    with RowCountCalculator {

  def list(candidate: String, uname: String, current: Option[String], installed: Option[String]) =
    Action.async(parse.anyContent) { request =>

      candidatesRepo.findCandidate(candidate).flatMap { candidateO =>

        val platform = Platform(uname).getOrElse(Platform.Universal)

        versionsRepo.findAllVersionsByCandidatePlatform(candidate, platform.identifier).map { versions =>

          import cats.syntax.show._

          def flat(os: Option[String]) = os.filter(_.nonEmpty)

          val rowCount = asRowCount(versions.length)
          val upperBound = rowCount * DefaultColumnCount
          val padded = pad(items(available(versions), local(flat(installed)), flat(current)).descendingOrder, upperBound)
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
}