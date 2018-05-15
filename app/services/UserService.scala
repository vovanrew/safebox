package services

import dto.User
import com.github.t3hnar.bcrypt._
import com.google.inject._
import repository.Users
import User.repo2Dto

@Singleton
class UserService {

  def createUser(user: User): Option[User] = {
    Users.findByEmail(user.email).orElse(Users.findByName(user.name)) match {
      case None =>
        Some(Users.create(user.name, user.email, hashePassword(user), generateSalt(user)))

      case Some(_) =>
        None
    }
  }

  def validateUser(user: User): Option[User] = {
    Users.findByEmail(user.email).orElse(Users.findByName(user.name)) match {
      case Some(u) if user.password.isBcrypted(u.password) =>
        Some(u)

      case _ => None
    }
  }

  def getUserWithName(name: String): Option[User] = {
    Users.findByName(name) match {
      case Some(u) =>
        Some(u)

      case _ => None
    }
  }

  //use if checked that user data is unique
  private def generateSalt(user: User): String = {
    (user.name + user.email + user.password).bcrypt
  }

  private def hashePassword(user: User): String = {
    user.password.bcrypt(generateSalt(user))
  }
}
