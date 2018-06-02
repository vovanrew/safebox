package dto


import java.time.LocalDateTime
import java.util.Date

import play.api.libs.json._
import repository.Files

case class FileMetadata(id: Long,
                        userId: Long,
                        filename: String,
                        description: String,
                        createdAt: LocalDateTime,
                        urlIdentifier: String,
                        isSecured: Boolean,
                        accessKey: String)

object FileMetadata {

  implicit val fileMetadataFormat: OFormat[FileMetadata] = Json.format[FileMetadata]
  implicit def fileRepo2Dto(file: Files): FileMetadata =
    FileMetadata(file.id, file.userId, file.filename, file.description, file.createdAt, file.urlIdentifier, file.isSecured, file.accessKey)

}
