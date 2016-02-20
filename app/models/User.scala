package models

import com.github.t3hnar.bcrypt._

case class User(email: String, encryptedPassword: String)

object User extends RedisModel {

  def fromMap(attrs: Map[String, String]): User = {
    User(attrs.getOrElse("email", ""),
         attrs.getOrElse("encryptedPassword", ""))
  }

  def authenticate(email: String, password: String): Boolean = User.findByEmail(email) map { user =>
    PasswordUtil.authenticate(password, user.encryptedPassword)
  } getOrElse false

  def findByEmail(email: String): Option[User] = redis.hgetall("user:" + email.toLowerCase) map { m =>
    if (m.nonEmpty) Some(fromMap(m)) else None
  } getOrElse None

  def create(email: String, password: String): Boolean = {
    val encryptedPassword = PasswordUtil.encryptPassword(password)
    val lowerEmail = email.toLowerCase
    val attrs = Map("email" -> lowerEmail, "encryptedPassword" -> encryptedPassword)
    createOrUpdateHash("user:" + lowerEmail, attrs)
  }

  def delete(email: String): Option[Long] = {
    val lowerEmail = email.toLowerCase
    redis.del(s"user:$lowerEmail")
  }

}

// http://codahale.com/how-to-safely-store-a-password/
object PasswordUtil {

  def authenticate(attemptedPassword: String, encryptedPassword: String) = {
    attemptedPassword.isBcrypted(encryptedPassword)
  }

  def encryptPassword(password: String) = {
    password.bcrypt
  }

}
