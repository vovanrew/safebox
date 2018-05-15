package dto

import play.api.libs.json.{Json, OFormat}
import repository.Users

case class User(id: Long = 0, name: String, email: String, password: String = "")

object User {

  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit def repo2Dto(repo: Users): User = User(repo.id, repo.name, repo.email)
  implicit def user2JsonStrFormat(user: User): String = Json.toJson(user).toString()

}
