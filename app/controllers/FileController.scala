package controllers

import com.google.inject._
import dto.Error._
import dto.FileMetadata._
import dto.JsonResult._
import dto._
import play.api.libs.json.{JsError, JsSuccess, Json}
import services.FileManagerService

import scala.concurrent.Future
import dto.User._
import play.api.mvc.Results.Unauthorized

import scala.concurrent.ExecutionContext.Implicits.global

class FileController @Inject() (scc: SecuredControllerComponents,
                                fileManager: FileManagerService) extends SecuredController(scc) {

  def upload = AuthenticatedAction(parse.multipartFormData).async { request =>
    Future (
      request.body.file("newFile").map { multipart =>
        fileManager.saveUploadedTempFile(multipart, request.session.get("id").get.toString) match {
          case Some(fileMetadata) =>
            Ok(Json.toJson(JsonResult(Success.name, Json.toJson(fileMetadata).toString())))
          case None =>
            InternalServerError(Json.toJson(JsonResult(Unsuccess.name, Error("File could not be uploaded."))))
        }

      }.getOrElse {
        BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Missing file"))))
      }
    )
  }

  def metaUpload = AuthenticatedAction(parse.json).async( request =>
    Future (
      request.body.validate[FileMetadata] match {
        case e: JsError =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Json validation error"))))

        case fileMetadata: JsSuccess[FileMetadata] =>
          val userId = request.session.get("id").get
          val fileData = fileMetadata.get
          fileManager.updateUserUploadedFileData(userId, fileData.filename, fileData.description, fileData.isSecured) match {
            case None =>
              BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Something went wrong try again."))))

            case Some(f) =>
              Ok(Json.toJson(JsonResult(Success.name, Json.toJson(f).toString())))
          }
      }
    )
  )

  def files = AuthenticatedAction.async { request =>
    Future (
      request.session.get("id") match {
        case Some(id) =>
          val files = fileManager.getUserFiles(id.toLong)
          Ok(Json.toJson(JsonResult(Success.name, Json.toJson(files).toString())))

        case _ =>
          Unauthorized("Authorize for this type requests.")
      }
    )
  }

  def file = Action(parse.json).async { request =>
    Future (
      request.body.validate[FileMetadata] match {
        case e: JsError =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Json validation error"))))

        case fileMetadata: JsSuccess[FileMetadata] =>
          val fileData = fileMetadata.get

          fileManager.getUserFileByIdentifierSecured(fileData.urlIdentifier, fileData.accessKey) match {
            case None =>
              BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Something went wrong try again."))))

            case Some(f) =>
              Ok.sendFile(f)
          }
      }
    )
  }

  def metaDownload(file: String) = Action.async { request =>
    Future (
      fileManager.getUserFileByIdentifier(file) match {
        case None =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Something went wrong try again."))))

        case Some(f) =>
          Ok(Json.toJson(JsonResult(Success.name, Json.toJson(f.copy(accessKey = "")).toString())))
      }
    )
  }

  def delete = AuthenticatedAction(parse.json).async { request =>
    Future(
      request.body.validate[FileMetadata] match {
        case e: JsError =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Json validation error"))))

        case fileMetadata: JsSuccess[FileMetadata] =>
          val fileData = fileMetadata.get
          fileManager.deleteFile(fileData) match {
            case None =>
              BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Something went wrong try again."))))

            case Some(f) =>
              Ok
          }
      }
    )
  }

}
