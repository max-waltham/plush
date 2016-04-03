package models

import com.redis._
import play.api.Play
import play.api.Play.current

trait RedisConnection {

  def get(name: String): Option[String] = Play.configuration.getString(name)

  val host = get("redis.REDIS_URL").getOrElse("localhost")
  val port = get("redis.REDIS_PORT").map(_.toInt).getOrElse(6379)
  val db = get("redis.REDIS_DB").map(_.toInt).getOrElse(0)
  val secr = get("redis.REDIS_SECRET")

  def redis = new RedisClient(host, port, db, secr)
}

trait RedisModel extends RedisConnection {

  def createOrUpdateHash(key: String, hash: Map[String, Any]): Boolean = {
    val noneFields = noneFieldsFromMap(hash)
    val valuesHash = valuesMapFromMap(hash)

    redis.pipeline { p =>
      if (noneFields.nonEmpty) p.hdel(key, noneFields.head, noneFields.tail.toSeq: _*)
      p.hmset(key, valuesHash)
    } match {
      // TODO: there should be a better way!
      case Some(List(Some(_), true)) => true
      case Some(List(true)) => true
      case _ => false
    }
  }

  def noneFieldsFromMap(m: Map[String, Any]) =
    m collect { case (k, None) => k }

  def valuesMapFromMap(m: Map[String, Any]) =
    (m -- noneFieldsFromMap(m)) map { case (k, Some(v)) => k -> v; case (k, v) => k -> v }

}
