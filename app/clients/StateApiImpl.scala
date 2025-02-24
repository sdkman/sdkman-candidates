package clients

import io.sdkman.repos.Version
import play.api.libs.json.{JsError, JsSuccess}
import utils.{JsonConverters, RequestBuilder}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StateApi {
  def findVersionsByCandidateAndPlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]]
}

@Singleton
class StateApiImpl @Inject() (requestBuilder: RequestBuilder) extends StateApi with JsonConverters {

  def findVersionsByCandidateAndPlatform(
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
}
