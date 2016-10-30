package db

import javax.inject.Singleton

import com.google.inject.Inject
import com.mongodb.ConnectionString
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCredential}
import play.api.Configuration

import scala.collection.JavaConversions._

@Singleton
class MongoConnectivity @Inject()(configuration: Configuration) {

  lazy val mongoUrl = configuration.getString("mongo.url").getOrElse("mongo://localhost:27017")

  lazy val userName = configuration.getString("mongo.username").getOrElse("")

  lazy val password = configuration.getString("mongo.password").getOrElse("")

  lazy val databaseName = configuration.getString("mongo.database").getOrElse("sdkman")

  def credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray)

  lazy val clusterSettings = ClusterSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUrl))
    .build()

  lazy val clientSettings = MongoClientSettings.builder()
    .credentialList(List(credential))
    .clusterSettings(clusterSettings)
    .build()

  lazy val mongoClient = if(userName.isEmpty) MongoClient(mongoUrl) else MongoClient(clientSettings)

  def db = mongoClient.getDatabase("sdkman")

  def appCollection = db.getCollection("application")

}

