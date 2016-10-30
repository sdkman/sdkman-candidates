package db

import javax.inject.Singleton

import org.mongodb.scala.MongoClient

@Singleton
class MongoConnectivity {

  lazy val mongoClient = MongoClient("mongodb://localhost/")

  def db = mongoClient.getDatabase("sdkman")

  def appCollection = db.getCollection("application")

}

