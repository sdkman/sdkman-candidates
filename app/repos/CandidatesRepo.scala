package repos

import com.google.inject.Inject
import db.MongoConnectivity
import org.mongodb.scala._
import org.mongodb.scala.bson._

import scala.concurrent.Future

class CandidatesRepo @Inject()(mongoConnectivity: MongoConnectivity) {
  def findAllCandidates(): Future[Seq[Candidate]] =
    mongoConnectivity
      .candidatesCollection
      .find()
      .map(implicit d =>
        Candidate(
          field("candidate"),
          field("name"),
          field("description"),
          field("default"),
          field("websiteUrl"),
          field("distribution")))
      .toFuture()

  private def field(n: String)(implicit d: Document) = d.get[BsonString](n).map(_.asString.getValue).get

}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)
