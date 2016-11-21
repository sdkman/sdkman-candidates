package repos

import db.MongoConnectivity
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import support.Mongo

class CandidatesRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OneAppPerSuite {

  val mongoConnectivity = new MongoConnectivity(app.configuration)
  val repo = new CandidatesRepo(mongoConnectivity)

  val scala = Candidate("scala", "Scala", "The Scala Language", "2.12.0", "http://www.scala-lang.org/", "UNIVERSAL")
  val groovy = Candidate("groovy", "Groovy", "The Groovy Language", "2.4.7", "http://www.groovy-lang.org/", "UNIVERSAL")
  val java = Candidate("java", "Java", "The Java Language", "8u111", "https://www.oracle.com", "MULTI_PLATFORM")

  "candidates repository" should {

    "find all candidates regardless of distribution" in {
      whenReady(repo.findAllCandidates()) { candidates =>
        candidates.size shouldBe 3
        candidates should contain(scala)
        candidates should contain(groovy)
        candidates should contain(java)
      }
    }

    "find candidates in alphabetically sorted order" in {
      whenReady(repo.findAllCandidates()) { candidates =>
        candidates.size shouldBe 3
        candidates(0) shouldBe groovy
        candidates(1) shouldBe java
        candidates(2) shouldBe scala
      }
    }

    "find some single candidate when searching by know candidate identifier" in {
      val candidate = "java"
      whenReady(repo.findByIdentifier(candidate)) { maybeCandidate =>
        maybeCandidate shouldBe defined
        maybeCandidate.foreach(_.candidate shouldBe candidate)
      }
    }

    "find none when searching by unknown candidate identifier" in {
      val candidate = "scoobeedoo"
      whenReady(repo.findByIdentifier(candidate)) { maybeCandidate =>
        maybeCandidate shouldNot be(defined)
      }
    }
  }

  before {
    Mongo.dropAllCollections()
    Mongo.insertCandidates(Seq(scala, groovy, java))
  }

}
