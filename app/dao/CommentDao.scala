package dao

import java.util.Date
import javax.inject.Inject

import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps //TODO remove

class CommentDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDao: UserDao, protected val postDao: PostDao) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dao.ExtPostgresDriver.api._


  //TODO move to sep class
  implicit val JavaUtilDateMapper =
    MappedColumnType.base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))


  val comments = TableQuery[Comments]

  private val queryByPostId = Compiled(
    (postId: Rep[Long]) => comments.filter(_.postId === postId))

  private val queryById = Compiled(
    (id: Rep[Long]) => comments.filter(_.id === id))

  def lookupByPostId(postId: Long): Future[Seq[Comment]] = {
    db.run(queryByPostId(postId).result)
  }

  def lookupById(id: Long): Future[Option[Comment]] = {
    db.run(queryById(id).result.headOption)
  }

  def commentsDetails(postId: Long) = {
    val q = (for {
      (c, u) <- comments joinLeft userDao.users on (_.userId === _.id)
    } yield (c, u)).filter(cu => cu._1.postId === postId)

    val result: Future[Seq[(Comment, Option[User])]] = db.run(q.result)
    result map(s => s map{case (c, u) => CommentDetails(c, u)})

  }

  def commentDetail(id: Long): Future[Option[CommentDetails]] = {
    val q = (for {
      (c, u) <- comments joinLeft userDao.users on (_.userId === _.id)
    } yield (c, u)).filter(cu => cu._1.id === id)

    val result = db.run(q.result.headOption)
    result.map {
      case Some((c, u)) => Some(CommentDetails(c, u))
      case None => None
    }
  }

  def create(c: PostedComment, ip: String)(implicit user: Option[User], executionContext: ExecutionContext): Future[CommentDetails] = {
    val comment = new Comment(c, user, ip)

    val commentId = db.run((for {
      postExist <- postDao.posts.filter(r => r.id === c.postId).exists.result if postExist
      commentId <- (comments returning comments.map(_.id)) += comment
      post <- postDao.queryById(c.postId).result.head
      updatedPost = post.copy(commentsNumber = post.commentsNumber + 1)
      updated <- postDao.queryById(c.postId).update(updatedPost)
    } yield commentId).transactionally)

    for {
      id <- commentId
      cd <- commentDetail(id)
    } yield cd.get

  }

  class Comments(tag: Tag) extends Table[Comment](tag, "comments") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def parentId = column[Option[Long]]("parentid")
    def topParentId = column[Option[Long]]("topparentid")
    def postId = column[Long]("postid")
    def userId = column[Option[Long]]("userid")
    def guest = column[Option[String]]("guest")
    def body = column[String]("body")
    def created = column[Date]("created")
    def ip = column[String]("ip")

    def parent = foreignKey("comments_parentid_fkey", parentId, comments)(_.id)
    def topParent = foreignKey("comments_topparentid_fkey", topParentId, comments)(_.id)
    def post = foreignKey("comments_postId_fkey", postId, postDao.posts)(_.id)
    def user = foreignKey("comments_userId_fkey", userId, userDao.users)(_.id)

    def * = (id, parentId, topParentId, postId, userId, guest, body, created, ip) <> (Comment.tupled, Comment.unapply)
  }

}

