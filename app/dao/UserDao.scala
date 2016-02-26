package dao

import java.util.Date
import controllers.register.AspiringUser
import org.mindrot.jbcrypt.BCrypt
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import models.{Sex, Role, User}

import scala.language.postfixOps

class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dao.ExtPostgresDriver.api._


  //TODO move to sep class
  implicit val roleColumnType =
    MappedColumnType.base[Role, String] (
      r => Role.stringOf(r),
      s => Role.valueOf(s))

  implicit val sexColumnType =
    MappedColumnType.base[Sex, Char] (
      s => Sex.charOf(s),
      c => Sex.valueOf(c))

  implicit val JavaUtilDateMapper =
    MappedColumnType.base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))


  val users = TableQuery[Users]

  private val queryById = Compiled(
    (id: Rep[Long]) => users.filter(_.id === id))

  private val queryByEmail = Compiled(
    (email: Rep[String]) => users.filter(_.email === email))

  private val queryByNick = Compiled(
    (nick: Rep[String]) => users.filter(_.nick.toLowerCase === nick.toLowerCase))

  def lookup(id: Long): Future[Option[User]] = {
    db.run(queryById(id).result.headOption)
  }

  def lookup(email: String): Future[Option[User]] = {
    db.run(queryByEmail(email).result.headOption)
  }

  def lookupByNick(nick: String): Future[Option[User]] = {
    db.run(queryByNick(nick).result.headOption)
  }

  def update(user:User) = {
    db.run(queryById(user.id).update(user))
  }

  def create(u: AspiringUser)(implicit executionContext: ExecutionContext): Future[Option[User]] = {
    val user = new User(u)

    db.run((for {
      userId <- (users returning users.map(_.id)) += user
      maybeUser <- queryById(userId).result.headOption
      } yield maybeUser).transactionally)
  }

  def authenticate(email: String, password: String): Option[User] = {

    val user = Await.result(lookup(email), 5 seconds)

    user match {
      case Some(u) if BCrypt.checkpw(password, u.password) => Some(u)
      case _ => None
    }

  }

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def nick = column[String]("nick")
    def email = column[String]("email")
    def password = column[String]("password")
    def created = column[Date]("created")
    def role = column[Role]("role")
    def sex = column[Sex]("sex")

    def idx = index("users_email_key", email, unique = true)
    def * = (id, nick, email, password, created, role, sex) <> (User.tupled, User.unapply)
  }

}

