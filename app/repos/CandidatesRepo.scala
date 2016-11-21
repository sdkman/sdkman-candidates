package repos

import com.google.inject.Inject
import db.MongoConnectivity
import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Sorts.ascending

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatesRepo @Inject()(mongoConnectivity: MongoConnectivity) {

  private implicit def documentToCandidate(doc: Document): Candidate =
    Candidate(
      field("candidate", doc),
      field("name", doc),
      field("description", doc),
      field("default", doc),
      field("websiteUrl", doc),
      field("distribution", doc))

  def findByIdentifier(candidate: String): Future[Option[Candidate]] =
    mongoConnectivity.candidatesCollection
      .find(Filters.eq("candidate", candidate))
      .first
      .map(doc => doc: Candidate)
      .toFuture()
      .map(_.headOption)

  def findAllCandidates(): Future[Seq[Candidate]] =
    mongoConnectivity.candidatesCollection
      .find().sort(ascending("candidate"))
      .map(doc => doc: Candidate)
      .toFuture()

  private def field(n: String, d: Document) = d.get[BsonString](n).map(_.asString.getValue).get

}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)
