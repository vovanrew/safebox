package repository

import java.time.LocalDateTime
import java.util.Date

import scalikejdbc._
import scalikejdbc.config.DBs

case class Files(id: Long,
                 userId: Long,
                 filename: String,
                 description: String,
                 path: String,
                 createdAt: LocalDateTime,
                 urlIdentifier: String,
                 initVector: String,
                 isSecured: Boolean,
                 accessKey: String)


object Files extends SQLSyntaxSupport[Files] {
  DBs.setupAll()

  def apply(c: SyntaxProvider[Files])(rs: WrappedResultSet): Files = apply(c.resultName)(rs)

  def apply(c: ResultName[Files])(rs: WrappedResultSet): Files = new Files(
    id = rs.get(c.id),
    userId = rs.get(c.userId),
    filename = rs.get(c.filename),
    description = rs.get(c.description),
    path = rs.get(c.path),
    createdAt = rs.get(c.createdAt),
    urlIdentifier = rs.get(c.urlIdentifier),
    initVector = rs.get(c.initVector),
    isSecured = rs.get(c.isSecured),
    accessKey = rs.get(c.accessKey)
  )

  val f = Files.syntax("files")

  def create(userId: Long,
            filename: String,
            description: String,
            path: String,
            createdAt: LocalDateTime,
            urlIdentifier: String,
            initVector: String,
            isSecured: Boolean,
            accessKey: String)(implicit session: DBSession = autoSession): Files = {

    val id = withSQL {
      insert.into(Files).namedValues(
        column.userId -> userId,
        column.filename -> filename,
        column.description -> description,
        column.path -> path,
        column.createdAt -> createdAt,
        column.urlIdentifier -> urlIdentifier,
        column.initVector -> initVector,
        column.isSecured -> isSecured,
        column.accessKey -> accessKey
      )
    }.updateAndReturnGeneratedKey().apply()

    Files(id, userId, filename, description, path, createdAt, urlIdentifier, initVector, isSecured, accessKey)
  }

  def filesByUserId(userId: Long)(implicit session: DBSession = autoSession): List[Files] = withSQL {
    select.from(Files as f).where.eq(f.userId, userId)
  }.map(Files(f)).list().apply()

  def findUserFile(userId: Long, filename: String)(implicit session: DBSession = autoSession): Option[Files] = withSQL {
    select.from(Files as f).where.eq(f.userId, userId).and.eq(f.filename, filename)
  }.map(Files(f)).single().apply()

  def updateUserFileMetadata(userId: Long, fileName: String, description: String, initVector: String, isSecured: Boolean, accessKey: String)
                            (implicit session: DBSession = autoSession): Int = withSQL {
    update(Files as f).set(
      f.description -> description,
      f.initVector -> initVector,
      f.isSecured -> isSecured,
      f.accessKey -> accessKey)
      .where.eq(f.userId, userId).and.eq(f.filename, fileName)
  }.update().apply()

  def deleteUserFileMetadata(userId: Long, filename: String)(implicit session: DBSession = autoSession) = withSQL {
    delete.from(Files as f).where.eq(f.userId, userId).and.eq(f.filename, filename)
  }

  def deleteFileById(fileId: Long)(implicit session: DBSession = autoSession) = withSQL {
    delete.from(Files as f).where.eq(f.id, fileId)
  }.update().apply()

  def getUserFileByIdentifierAndKey(identifier: String, key: String)(implicit session: DBSession = autoSession): Option[Files] = withSQL {
    select.from(Files as f).where.eq(f.urlIdentifier, identifier).and.eq(f.accessKey, key)
  }.map(Files(f)).single().apply()

  def getFileByIdentifier(identifier: String)(implicit session: DBSession = autoSession): Option[Files] = withSQL {
    select.from(Files as f).where.eq(f.urlIdentifier, identifier)
  }.map(Files(f)).single().apply()
}
