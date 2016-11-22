package repos

import com.google.inject.Inject
import db.MongoConnectivity
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Sorts.ascending

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatesRepo @Inject()(mongo: MongoConnectivity) {
  def findByIdentifier(candidate: String): Future[Option[Candidate]] =
    mongo.candidatesCollection
      .find(equal("candidate", candidate))
      .first
      .map(doc => doc: Candidate)
      .toFuture()
      .map(_.headOption)

  def findAllCandidates(): Future[Seq[Candidate]] =
    mongo.candidatesCollection
      .find()
      .sort(ascending("candidate"))
      .map(doc => doc: Candidate)
      .toFuture()
}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)
