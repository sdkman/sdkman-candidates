package clients

import cats.implicits.{catsSyntaxOptionId, none}
import io.sdkman.repos.Version
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess}
import utils.JsonConverters

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StateApiVersion(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    visible: Boolean,
    vendor: Option[String]
)

trait StateApi {

  def findVisibleVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]]

  def findVersionByCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[StateApiVersion]]
}

@Singleton
class StateApiImpl @Inject() (requestBuilder: RequestBuilder) extends StateApi with JsonConverters {

  override def findVisibleVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]] =
    requestBuilder
      .versionsByCandidatePlatformRequest(candidate, platform)
      .get()
      .flatMap { response =>
        response.json.validate[List[Version]] match {
          case JsSuccess(value, _) => Future.successful(value)
          case JsError(e)          =>
            // TODO: improve error handling
            Future.failed(new RuntimeException(e.toString))
        }
      }

  override def findVersionByCandidateAndPlatform(
      candidate: String,
      version: String,
      platform: String,
      vendor: Option[String]
  ): Future[Option[StateApiVersion]] =
    requestBuilder
      .versionByCandidatePlatformRequest(candidate, version, platform, vendor)
      .get()
      .flatMap { response =>
        if (response.status == Status.OK) response.json.validate[StateApiVersion] match {
          case JsSuccess(value, _) => Future.successful(value.some)
          case JsError(e)          => Future.failed(new RuntimeException(e.toString))
        }
        else Future.successful(none)
      }
}
