package clients

import cats.implicits.{catsSyntaxOptionId, none}
import domain.{Candidate, Version}
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess}
import utils.JsonConverters

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StateApi {

  def findAllCandidates(): Future[Seq[Candidate]]

  def findVisibleVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]]

  def findVersionByCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[Version]]

  def findVersionByCandidateAndTag(
      candidate: String,
      tag: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[Version]]
}

@Singleton
class StateApiImpl @Inject() (requestBuilder: RequestBuilder)
    extends StateApi
    with JsonConverters
    with Logging {

  import StateApiImpl.{CandidatesContractDrift, CandidatesUnavailable, ContractDrift}

  override def findAllCandidates(): Future[Seq[Candidate]] =
    requestBuilder
      .candidatesRequest()
      .get()
      .flatMap { response =>
        if (response.status == Status.OK)
          response.json.validate[List[Candidate]] match {
            // The live registry is never legitimately empty, so an empty 200
            // is drift, not a valid empty set the caller could cache.
            case JsSuccess(Nil, _)   => Future.failed(CandidatesContractDrift("empty array"))
            case JsSuccess(value, _) => Future.successful(value)
            case JsError(e)          => Future.failed(CandidatesContractDrift(e.toString))
          }
        else Future.failed(CandidatesUnavailable(response.status))
      }

  override def findVisibleVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]] =
    requestBuilder
      .versionsByCandidatePlatformRequest(candidate, platform)
      .get()
      .flatMap { response =>
        if (response.status == Status.OK)
          response.json.validate[List[Version]] match {
            case JsSuccess(value, _) => Future.successful(value)
            // A 200 whose body is not Version[] is contract drift, not "no
            // versions": surface it as a distinct failure the degrade path
            // below deliberately does not swallow.
            case JsError(e) =>
              Future.failed(ContractDrift(candidate, platform, e.toString))
          }
        else {
          // Any non-2xx degrades to an empty listing. Reachable today for the
          // platforms the State API rejects with 400 (FREE_BSD/SUN_OS, behind
          // `freebsd`/`sunos`), and covers 500/503 blips too.
          logger.warn(
            s"State API version listing for $candidate/$platform returned ${response.status}; degrading to empty list"
          )
          Future.successful(Seq.empty[Version])
        }
      }
      .recover {
        // Timeouts and transport failures degrade as well; contract drift on a
        // 200 body is intentionally excluded from the guard so it still fails.
        case e if !e.isInstanceOf[ContractDrift] =>
          logger.warn(
            s"State API version listing for $candidate/$platform failed; degrading to empty list",
            e
          )
          Seq.empty[Version]
      }

  override def findVersionByCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[Version]] =
    requestBuilder
      .versionByCandidatePlatformRequest(candidate, version, platform, vendor)
      .get()
      .flatMap { response =>
        if (response.status == Status.OK) response.json.validate[Version] match {
          case JsSuccess(value, _) => Future.successful(value.some)
          case JsError(e)          =>
            // TODO: improve error handling
            Future.failed(new RuntimeException(e.toString))
        }
        else Future.successful(none)
      }

  override def findVersionByCandidateAndTag(
      candidate: String,
      tag: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[Version]] =
    requestBuilder
      .versionByCandidateTagRequest(candidate, tag, platform, vendor)
      .get()
      .flatMap { response =>
        if (response.status == Status.OK) response.json.validate[Version] match {
          case JsSuccess(value, _) => Future.successful(value.some)
          case JsError(e)          =>
            // TODO: improve error handling
            Future.failed(new RuntimeException(e.toString))
        }
        else Future.successful(none)
      }
}

object StateApiImpl {

  // A 200 candidate registry response that is empty or does not parse as
  // Candidate[]. Callers retain their previous set rather than adopt it.
  final case class CandidatesContractDrift(detail: String)
      extends RuntimeException(
        s"State API candidate registry returned 200 with an unusable body: $detail"
      )

  // Any non-200 from the candidate registry read.
  final case class CandidatesUnavailable(status: Int)
      extends RuntimeException(s"State API candidate registry returned status $status")

  // A 200 listing response whose body does not parse as Version[] signals the
  // State API contract has drifted. It is distinct from a "no versions"
  // outcome, so the listing degrade path leaves this failure to propagate.
  private final case class ContractDrift(candidate: String, platform: String, detail: String)
      extends RuntimeException(
        s"State API version listing for $candidate/$platform returned 200 with a body that is not Version[]: $detail"
      )
}
