package db

import javax.inject.Singleton

import com.google.inject.Inject
import org.mongodb.scala.MongoClient
import play.api.Configuration

@Singleton
class MongoConnectivity @Inject()(configuration: Configuration) {

  lazy val mongoUrl = configuration.getString("mongo.url").getOrElse("mongo://localhost:27017")

  lazy val mongoClient = MongoClient(mongoUrl)

  def db = mongoClient.getDatabase("sdkman")

  def appCollection = db.getCollection("application")

}

