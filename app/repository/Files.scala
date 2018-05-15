package repository

import akka.http.scaladsl.model.DateTime
import repository.Users.autoSession
import scalikejdbc._

case class Files(id: Long,
                 userId: Long,
                 filename: String,
                 path: String,
                 createdAt: DateTime,
                 urlIdentifier: String,
                 accessKey: String)


object Files extends SQLSyntaxSupport[Files] {

  def apply(c: SyntaxProvider[Files])(rs: WrappedResultSet): Files = apply(c.resultName)(rs)

  def apply(c: ResultName[Files])(rs: WrappedResultSet): Files = new Files(
    id = rs.get(c.id),
    userId = rs.get(c.userId),
    filename = rs.get(c.filename),
    path = rs.get(c.path),
    createdAt = rs.get(c.createdAt),
    urlIdentifier = rs.get(c.urlIdentifier),
    accessKey = rs.get(c.accessKey)
  )

  val f = Files.syntax("files")

  def create(userId: Long,
            filename: String,
            path: String,
            createdAt: DateTime,
            urlIdentifier: String,
            accessKey: String)(implicit session: DBSession = autoSession): Files = {

    val id = withSQL {
      insert.into(Files).namedValues(
        column.userId -> userId,
        column.filename -> filename,
        column.path -> path,
        column.createdAt -> createdAt,
        column.urlIdentifier -> urlIdentifier,
        column.accessKey -> accessKey
      )
    }.updateAndReturnGeneratedKey().apply()

    Files(id, userId, filename, path, createdAt, urlIdentifier, accessKey)
  }

  def filesByUserId(userId: Long)(implicit session: DBSession = autoSession): List[Files] = withSQL {
    select.from(Files as f).where.eq(f.userId, userId)
  }.map(Files(f)).list().apply()

  def updateAccessKey(id: Long, newKey: String)(implicit session: DBSession = autoSession): Int = withSQL {
    update(Files as f).set(f.accessKey -> newKey).where.eq(f.id, id)
  }.update().apply()
}
