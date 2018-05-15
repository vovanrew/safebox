package controllers

import com.google.inject._
import dto.Error._
import dto.FileMetadata._
import dto.JsonResult._
import dto.{Error, JsonResult, Success, Unsuccess}
import play.api.libs.json.Json
import services.FileManagerService

class FileController @Inject() (scc: SecuredControllerComponents,
                                fileManager: FileManagerService) extends SecuredController(scc) {

  def upload = AuthenticatedAction(parse.multipartFormData) { request =>
    request.body.file("multipart").map { multipart =>

      fileManager.saveUploadedTempFile(multipart.ref, request.session.get("id").get.toString) match {
        case Some(fileMetadata) =>
          Ok(Json.toJson(JsonResult(Success.name, Json.toJson(fileMetadata).toString())))
        case None =>
          InternalServerError(Json.toJson(JsonResult(Unsuccess.name, Error("File could not be uploaded."))))
      }

    }.getOrElse {
      BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Missing file"))))
    }
  }

}
