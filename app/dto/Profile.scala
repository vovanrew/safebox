package dto

import play.api.libs.json.{Json, OFormat}

case class Profile(user: User, files: List[FileMetadata])

object Profile {
  import FileMetadata._
  import User._

  implicit val profileFormat: OFormat[Profile] = Json.format[Profile]

}
