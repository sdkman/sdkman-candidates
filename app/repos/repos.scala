package repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.ApplicationRepo
import javax.inject.Singleton

trait MongoConn extends MongoConnectivity with MongoConfiguration {
  override lazy val config: Config = ConfigFactory.load()
}

@Singleton
class ApplicationRepository extends ApplicationRepo with MongoConn
