package repos

import com.google.inject.Inject
import db.MongoConnectivity
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Document, ScalaObservable}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class VersionsRepo @Inject()(mongo: MongoConnectivity) {
  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    mongo.versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .map(implicit d => Version(field("candidate"), field("version"), field("platform"), field("url")))
      .toFuture()
      .map(_.headOption)

  private def field(n: String)(implicit d: Document) = d.get[BsonString](n).map(_.asString.getValue).get
}

case class Version(candidate: String, version: String, platform: String, url: String)