package utils

import play.api.Play
import play.api.Play.current

trait Conf {
  def get(name: String): Option[String] = Play.configuration.getString(name)
  def getInt(name: String): Option[Int] = Play.configuration.getInt(name)
  def getLong(name: String): Option[Long] = Play.configuration.getLong(name)
  def getBoolean(name: String): Option[Boolean] = Play.configuration.getBoolean(name)
  def getSeqString(name: String): Option[Seq[String]] = Play.configuration.getStringSeq(name)
}

object RedisConf extends Conf {
  val host = get("redis.url").getOrElse("localhost")
  val port = getInt("redis.port").getOrElse(6379)
  val db = getInt("redis.db").getOrElse(0)
  val secret = get("redis.secret")
}

object StorageConf extends Conf {
  val icons = get("storage.icons").getOrElse("icons")
  val certs = get("storage.certs").getOrElse("certs")
}
