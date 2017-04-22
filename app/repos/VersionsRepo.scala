package repos

import com.google.inject.Inject
import db.MongoConnectivity
import org.mongodb.scala.ScalaObservable
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.ascending

import scala.concurrent.Future

class VersionsRepo @Inject()(mongo: MongoConnectivity) {

  import repos.mongoExecutionContext

  def findAllVersions(candidate: String, platform: String): Future[Seq[Version]] =
    mongo.versionsCollection
      .find(and(equal("candidate", candidate), equal("platform", platform)))
      .sort(ascending("version"))
      .map(doc => doc: Version)
      .toFuture()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    mongo.versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .map(doc => doc: Version)
      .toFuture()
      .map(_.headOption)
}

case class Version(candidate: String, version: String, platform: String, url: String)