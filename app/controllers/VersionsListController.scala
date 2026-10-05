package controllers

import clients.{CandidateRegistry, StateApiImpl}
import domain.Platform
import ordering.VersionItemOrdering
import play.api.mvc._
import rendering.{RowCountCalculator, VersionItemListBuilder, VersionRendering, VersionRow}
import utils.VersionListProperties

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VersionsListController @Inject() (
    stateApi: StateApiImpl,
    registry: CandidateRegistry,
    cc: ControllerComponents
) extends AbstractController(cc)
    with VersionListProperties
    with VersionItemOrdering
    with VersionRendering
    with VersionItemListBuilder
    with RowCountCalculator {

  def list(
      candidateId: String,
      platformId: String,
      current: Option[String],
      installed: Option[String]
  ): Action[AnyContent] =
    Action.async(parse.anyContent) { _ =>
      val universalVersionsF = stateApi.findVisibleVersionsByCandidateAndPlatform(
        candidateId,
        Platform.Universal.name
      )
      val platformVersionsF = stateApi.findVisibleVersionsByCandidateAndPlatform(
        candidateId,
        Platform(platformId).name
      )

      def listing(title: String): Future[Result] = for {
        universalVersions <- universalVersionsF
        platformVersions  <- platformVersionsF
        allVersions = universalVersions ++ platformVersions
      } yield {
        import cats.syntax.show._

        def flattenNonEmpty(os: Option[String]) = os.filter(_.nonEmpty)

        val rowCount   = asRowCount(allVersions.length)
        val upperBound = rowCount * DefaultColumnCount
        val padded = pad(
          items(
            available(allVersions),
            local(flattenNonEmpty(installed)),
            flattenNonEmpty(current)
          ).descendingOrder,
          upperBound
        )
        val rows: Seq[String] = for {
          i <- 0 until rowCount
          col1 = padded(i + 0 * rowCount)
          col2 = padded(i + 1 * rowCount)
          col3 = padded(i + 2 * rowCount)
          col4 = padded(i + 3 * rowCount)

        } yield VersionRow(col1, col2, col3, col4).show

        Ok(views.txt.version_list(title, rows))
      }

      // The listing title is the candidate's display name from the registry;
      // no set is 503, an unknown candidate is 404
      // (specs/candidate-registry-read-flip.md, §GET /candidates/:candidate/:platformId/versions/list).
      registry.candidates().flatMap {
        case None => Future.successful(ServiceUnavailable)
        case Some(cs) =>
          cs.find(_.candidate == candidateId) match {
            case None            => Future.successful(NotFound)
            case Some(candidate) => listing(candidate.name)
          }
      }
    }
}
