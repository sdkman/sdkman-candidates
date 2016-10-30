package db

import javax.inject.Singleton

import com.mongodb.ConnectionString
import org.mongodb.scala.{MongoClient, MongoClientSettings}
import org.mongodb.scala.connection.ClusterSettings

@Singleton
class MongoConnectivity {

  val mongoSettings = MongoClientSettings.builder().build()

  val connectionString = new ConnectionString("mongodb://localhost/?connectTimeoutMS=5000&waitQueueTimeoutMS=500")

  val clusterSettings = ClusterSettings.builder()
    .applyConnectionString(connectionString)
    .build()

  val mongoClientSettings = MongoClientSettings.builder()
    .clusterSettings(clusterSettings)
    .build()

  lazy val mongoClient = MongoClient(mongoClientSettings)

  def db = mongoClient.getDatabase("sdkman")

}

