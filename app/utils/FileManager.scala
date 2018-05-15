package utils

import java.nio.file.Files
import java.nio.file.attribute.{BasicFileAttributes, DosFileAttributes, PosixFileAttributes}

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import sys.process._
import java.net.URL

import scala.reflect.io.Path

object FileManager {
  def attributes(path: Path): BasicFileAttributes = Files.readAttributes(path.jfile.toPath.normalize.toAbsolutePath, classOf[BasicFileAttributes])

  def posixAttributes(path: Path): PosixFileAttributes = Files.readAttributes(path.jfile.toPath.normalize.toAbsolutePath, classOf[PosixFileAttributes])

  def dosAttributes(path: Path): DosFileAttributes = Files.readAttributes(path.jfile.toPath.normalize.toAbsolutePath, classOf[DosFileAttributes])

  def getFullFileName(filename: String, origin: String) = filename + "." + origin.substring(origin.lastIndexOf(".") + 1)

  def saveFile(file: FilePart[TemporaryFile], path: Path, filenameFull: String) = {
    file.ref.moveTo((path.createDirectory() / filenameFull).jfile)
  }

  def destroyFile(path: Path, name: String): Unit = {
    val file = (path / name).toFile
    file.delete
  }
}
