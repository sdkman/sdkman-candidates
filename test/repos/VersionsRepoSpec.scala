package repos

import db.MongoConnectivity
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import org.scalatestplus.play.OneAppPerSuite
import support.Mongo

class VersionsRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues with OneAppPerSuite {

  val mongoConnectivity = new MongoConnectivity(app.configuration)

  val repo = new VersionsRepo(mongoConnectivity)

  "versions repository" should {

    "attempt to find one Version by candidate, version and platform" when {

      "that version is available" in {
        val candidate = "java"
        val version = "8u111"
        val platform = "LINUX_64"
        val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

        Mongo.insertVersion(Version(candidate, version, platform, url))

        whenReady(repo.findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.candidate shouldBe candidate
          maybeVersion.value.version shouldBe version
          maybeVersion.value.platform shouldBe platform
          maybeVersion.value.url shouldBe url
        }
      }

      "find no Version by candidate, version and platform" in {
        whenReady(repo.findVersion("java", "7u65", "LINUX_64")) { maybeVersion =>
          maybeVersion should not be defined
        }
      }
    }

    "find all versions by candidate and platform ordered by version" in {
      val java8u111 = Version("java", "8u111", "LINUX_64", "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz")
      val java8u121 = Version("java", "8u121", "LINUX_64", "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz")
      val java8u131 = Version("java", "8u131", "LINUX_64", "http://dl/8u131-b14/jdk-8u131-linux-x64.tar.gz")

      val javaVersions = Seq(java8u111, java8u121, java8u131)

      javaVersions.foreach(Mongo.insertVersion)

      whenReady(repo.findAllVersions("java", "LINUX_64")) { versions =>
        versions.size shouldBe 3
        versions(0) shouldBe java8u111
        versions(1) shouldBe java8u121
        versions(2) shouldBe java8u131
      }
    }
  }

  before {
    Mongo.dropAllCollections()
  }
}
