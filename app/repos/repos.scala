package repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.{ApplicationRepo, CandidatesRepo, VersionsRepo}
import javax.inject.Singleton


trait MongoConn extends MongoConnectivity with MongoConfiguration {
  override val config: Config = ConfigFactory.load()
}

@Singleton
class CandidatesRepository extends CandidatesRepo with MongoConn

@Singleton
class VersionsRepository extends VersionsRepo with MongoConn

@Singleton
class ApplicationRepository extends ApplicationRepo with MongoConn