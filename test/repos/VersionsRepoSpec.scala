package repos

import db.MongoConnectivity
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import support.Mongo

class VersionsRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OneAppPerSuite {

  val mongoConnectivity = new MongoConnectivity(app.configuration)
  val repo = new VersionsRepo(mongoConnectivity)

  "versions repository" should {

    val candidate = "java"
    val version = "8u111"
    val platform = "LINUX"
    val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

    "find one version by candidate, version and platform" in {

      Mongo.insertVersion(candidate, version, platform, url)

      whenReady(repo.findVersion(candidate, version, platform)) { maybe =>
        maybe.map {v =>
          v.candidate shouldBe candidate
          v.version shouldBe version
          v.platform shouldBe platform
          v.url shouldBe url

        } orElse fail("No version was found.")
      }
    }

    "find no version by candidate, version and platform" in {
      whenReady(repo.findVersion(candidate, version, platform)) { maybe =>
        maybe should not be defined
      }
    }
  }

  before {
    Mongo.dropAllCollections()
  }
}
