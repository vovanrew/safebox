package dto

import play.api.libs.json.{Json, OFormat}

case class Error(reason: String)

object Error {

  implicit val errorFormat: OFormat[Error] = Json.format[Error]
  implicit def error2JsonStrFormat(error: Error): String = Json.toJson(error).toString()

}

