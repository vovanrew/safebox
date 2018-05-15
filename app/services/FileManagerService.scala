package services

import com.google.inject._
import com.github.t3hnar.bcrypt._

import scala.reflect.io.Path
import java.io.{File => JFile}
import java.nio.file._
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files => JFiles}

import akka.http.scaladsl.model.DateTime
import repository.{Files => MetaFiles}
import com.typesafe.config.Config
import dto.FileMetadata
import org.apache.commons.lang3.SystemUtils
import play.api.libs.Files.TemporaryFile
import utils.Utils
import dto.FileMetadata._

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

  def saveUploadedTempFile(file: TemporaryFile, userId: String): Option[FileMetadata] = {
    val pathDir = (uploadPath / userId).createDirectory()
    val filename = file.getFileName.toString
    val fullPath = s"${pathDir.path}/$filename"
    file.moveTo(Paths.get(fullPath), replace = true)

    Some(MetaFiles.create(userId.toLong, filename, fullPath, DateTime.now, hashedFilename(filename, userId), ""))
  }

  private def hashedFilename(filename: String, userId: String): String = {
    (filename + userId).bcrypt
  }

  def downloadFile(url: String, toName: String => String, directoryName: String): (String, JFile) = {
    val filename = url.substring(url.lastIndexOf("/") + 1)
    val originalFileId = toName(filename).toLowerCase

    val pathDir = (uploadPath / directoryName).createDirectory()
    val filePath = pathDir / originalFileId

    Utils.fileDownloader(url, filePath)

    (filename, filePath.jfile)
  }

  def saveFile(file: JFile, directoryName: String, filenameFull: String, move: Boolean = false): JFile = {
    val pathDir = (uploadPath / directoryName).createDirectory()
    val jfile = (pathDir / filenameFull).jfile

    if (move) {
      JFiles.move(file.toPath, jfile.toPath)
    } else {
      JFiles.copy(file.toPath, jfile.toPath)
    }

    if (SystemUtils.IS_OS_LINUX) changePermissions(jfile)

    jfile
  }

  def abstractFullFile(directoryName: String, filenameFull: String): JFile = {
    val pathDir = (uploadPath / directoryName).createDirectory()
    (pathDir / filenameFull).jfile
  }

  private def changePermissions(jfile: JFile) = {
    try {
      Files.setPosixFilePermissions(
        jfile.toPath,
        JavaConversions.setAsJavaSet(
          Set(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ)
        )
      )
    } catch {
      case e: java.nio.file.attribute.UserPrincipalNotFoundException =>
      case e: java.lang.UnsupportedOperationException =>
    }
  }
}

