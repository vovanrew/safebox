package dto

import play.api.libs.json.{Json, OFormat}

case class JsonResult(status: String = Success.name, data: String)

case object JsonResult {

  implicit val jsonResultFormat: OFormat[JsonResult] = Json.format[JsonResult]

}

case object Success {
  val name = "successful"
}

case object Unsuccess {
  val name = "unsuccessful"
}