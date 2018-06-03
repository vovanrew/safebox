package services

import com.google.inject._
import com.github.t3hnar.bcrypt._

import scala.reflect.io.Path
import java.io.{File => JFile}
import java.nio.file._
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files => JFiles}
import java.time.LocalDateTime
import java.util.Date

import repository.{Files => MetaFiles}
import com.typesafe.config.Config
import dto.FileMetadata
import org.apache.commons.lang3.SystemUtils
import play.api.libs.Files.TemporaryFile
import utils.Utils
import dto.FileMetadata._
import play.api.mvc.MultipartFormData

import scala.collection.JavaConversions

@Singleton
class FileManagerService @Inject()(config: Config) {

  val uploadPath = Path(config.getString("uploadDir"))
  val webPath = config.getString("uploadDir")
  val hostnameUrl = config.getString("hostnameUrl")

  type WebPath = Long => String

  val mislaidPath: WebPath = "/storage/" + _.toString + "/files"

  def mislaidPhotoPath(mislaidId: Long): String => String = mislaidPath(mislaidId) + "/" + _

  def mislaidPhotoUrl(mislaidId: Long): String => String = hostnameUrl + webPath + mislaidPhotoPath(mislaidId)(_)

  def getUserFiles(userId: Long): List[FileMetadata] = MetaFiles.filesByUserId(userId).map(fileRepo2Dto)

  def saveUploadedTempFile(file: MultipartFormData.FilePart[TemporaryFile], userId: String): Option[FileMetadata] = {
    val pathDir = (uploadPath / userId).createDirectory()
    val filename = file.filename
    val fullPath = s"${pathDir.path}/$filename"
    val exists = Files.exists(Paths.get(fullPath))

    if (exists) {
      MetaFiles.filesByUserId(userId.toLong).headOption.map(fileRepo2Dto)
    } else {
      file.ref.moveTo(Paths.get(fullPath), replace = true)
      Some(MetaFiles.create(userId.toLong, filename, "", fullPath, LocalDateTime.now(), hashedFilename(filename, userId), "", false, ""))
    }
  }

  def updateUserUploadedFileData(userId: String, filename: String, description: String, initVector: String, isSecured: Boolean): Option[FileMetadata] = {
    val filePath = s"$uploadPath/$userId/$filename"
    val exists = Files.exists(Paths.get(filePath))

    if (exists) {
      val accessKey = if(isSecured) generateAccessKey(userId, filename) else ""

      MetaFiles.updateUserFileMetadata(userId.toLong, filename, description, initVector, isSecured, accessKey)
      MetaFiles.findUserFile(userId.toLong, filename).map(fileRepo2Dto)
    } else {
      MetaFiles.deleteUserFileMetadata(userId.toLong, filename)
      None
    }
  }

  def getUserFileByIdentifierSecured(identifier: String, key: String): Option[java.io.File] = {
    MetaFiles.getUserFileByIdentifierAndKey(identifier, key).map(f => new java.io.File(f.path))
  }

  def getUserFileByIdentifier(identifier: String): Option[FileMetadata] = {
    MetaFiles.getFileByIdentifier(identifier).map(fileRepo2Dto)
  }



  private def hashedFilename(filename: String, userId: String): String = {
    (filename + userId).bcrypt
  }

  private def generateAccessKey(userId: String, filename: String): String = {
    (userId + filename).bcrypt
  }

  def downloadFile(url: String, toName: String => String, directoryName: String): (String, JFile) = {
    val filename = url.substring(url.lastIndexOf("/") + 1)
    val originalFileId = toName(filename).toLowerCase

    val pathDir = (uploadPath / directoryName).createDirectory()
    val filePath = pathDir / originalFileId

    Utils.fileDownloader(url, filePath)

    (filename, filePath.jfile)
  }

  def deleteFile(fileMetadata: FileMetadata): Option[FileMetadata] = {
    MetaFiles.getFileByIdentifier(fileMetadata.urlIdentifier) match {
      case Some(fileRepo) =>
        val file = new JFile(fileRepo.path)
        file.delete()
        MetaFiles.deleteFileById(fileRepo.id)
        Some(fileMetadata)

      case None => None
    }
  }

}

