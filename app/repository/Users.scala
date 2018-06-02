package repository

import scalikejdbc._
import scalikejdbc.config._

case class Users(id: Long,
                 name: String,
                 email: String,
                 password: String,
                 salt: String)

object Users extends SQLSyntaxSupport[Users]{

  def apply(c: SyntaxProvider[Users])(rs: WrappedResultSet): Users = apply(c.resultName)(rs)
  def apply(c: ResultName[Users])(rs: WrappedResultSet): Users = new  Users(
    id = rs.get(c.id),
    name = rs.get(c.name),
    email = rs.get(c.email),
    password = rs.get(c.password),
    salt = rs.get(c.salt)
  )

  DBs.setupAll()
  val u = Users.syntax("users")

  def create(name: String, email: String, password: String, salt: String)(implicit session: DBSession = autoSession): Users = {
    val id = withSQL {
      insert.into(Users).namedValues(
        column.name -> name,
        column.email -> email,
        column.password -> password,
        column.salt -> salt
      )
    }.updateAndReturnGeneratedKey().apply()

    Users(id, name, email, password, salt)
  }

  def findByEmail(email: String)(implicit session: DBSession = autoSession): Option[Users] = withSQL {
    select.from(Users as u).where.eq(u.email, email)
  }.map(Users(u)).single.apply()

  def findByName(name: String)(implicit session: DBSession = autoSession): Option[Users] = withSQL {
    select.from(Users as u).where.eq(u.name, name)
  }.map(Users(u)).single.apply()

  def findById(id: Long)(implicit session: DBSession = autoSession): Option[Users] = withSQL {
    select.from(Users as u).where.eq(u.id, id)
  }.map(Users(u)).single.apply()
}
