package dto

import akka.http.scaladsl.model.DateTime
import play.api.libs.json.{Json, OFormat}
import repository.Files

case class FileMetadata(id: Long,
                        userId: Long,
                        filename: String,
                        createdAt: DateTime,
                        urlIdentifier: String,
                        accessKey: String)

object FileMetadata {

  implicit val fileMetadataFormat: OFormat[FileMetadata] = Json.format[FileMetadata]
  implicit def fileRepo2Dto(file: Files): FileMetadata =
    FileMetadata(file.id, file.userId, file.filename, file.createdAt, file.urlIdentifier, file.accessKey)

}
