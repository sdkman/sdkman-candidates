package repos

import db.MongoConnectivity
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import support.Mongo

class CandidatesRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OneAppPerSuite {

  val mongoConnectivity = new MongoConnectivity(app.configuration)
  val repo = new CandidatesRepo(mongoConnectivity)

  "candidates repository" should {
    "find all candidates regardless of distribution" in {
      val scala = Candidate("scala", "Scala", "The Scala Language", "2.12.0", "http://www.scala-lang.org/", "UNIVERSAL")
      val groovy = Candidate("groovy", "Groovy", "The Groovy Language", "2.4.7", "http://www.groovy-lang.org/", "UNIVERSAL")
      val java = Candidate("java", "Java", "The Java Language", "8u111", "https://www.oracle.com", "MULTI_PLATFORM")

      Mongo.insertCandidates(Seq(scala, groovy, java))

      whenReady(repo.findAllCandidates()) { candidates =>
        candidates should contain(scala)
        candidates should contain(groovy)
        candidates should contain(java)
      }
    }
  }

  before {
    Mongo.dropAllCollections()
  }

}
