package dao

import java.util.Date
import javax.inject.Inject

import controllers.posts.NotReadyPost
import models.{PostDetails, PostVote, User, Post}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PostDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDao: UserDao) extends HasDatabaseConfigProvider[JdbcProfile] {

  import dao.ExtPostgresDriver.api._

  implicit val javaUtilDateMapper =
    MappedColumnType .base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  val posts = TableQuery[Posts]
  private val postsVotes = TableQuery[PostVotes]

  val queryById = Compiled(
    (id: Rep[Long]) => posts.filter(_.id === id))

  private val countPosts = Compiled(posts.length)

  def lookup(id: Long): Future[Option[Post]] = {
    db.run(queryById(id).result.headOption)
  }

  def details(id: Long): Future[Option[(Post, User)]] = {
    val q = for {
      r <- posts.filter(_.id === id)
      u <- userDao.users if u.id === r.userId
    } yield (r, u)
    db.run(q.result.headOption)
  }

  private def searchPosts(params: SearchPostsParams) = {
    var q = for {
      r <- posts
      u <- userDao.users if u.id === r.userId
    } yield (r, u)

    if (params.visible.isDefined) {
      q = q.filter(f => f._1.visible === params.visible.get)
    }

    if (params.nick.isDefined) {
      q = q.filter(f => f._2.nick === params.nick.get)
    }

    if (params.tag.isDefined) {
      q = q.filter(t => params.tag.bind === t._1.tags.any)
    }

    q = q.sortBy(_._1.created.desc)

    if (params.offset.isDefined) {
      q = q.drop(params.offset.get)
    }

    if (params.limit.isDefined) {
      q = q.take(params.limit.get)
    }

    q
  }
  def all(params: SearchPostsParams): Future[Seq[PostDetails]] = {
    val result: Future[Seq[(Post, User)]] = db.run(searchPosts(params).result)

    result.map( f => f.map{ case (post, user) => new PostDetails(post, user) } )
  }

  def count(params: SearchPostsParams): Future[Int] = {
    db.run(searchPosts(params).length.result)
  }

  def update(post: Post) = {
    db.run(queryById(post.id).update(post))
  }

  def create(r: NotReadyPost)(implicit user: User, executionContext: ExecutionContext): Future[Post] = {
    val post = new Post(r, user.id)

    db.run((for {
      postId <- (posts returning posts.map(_.id)) += post
      maybePost <- queryById(postId).result.headOption
    } yield maybePost.get).transactionally)
  }

  def createVoteIfNotVoted(vote: PostVote)(implicit executionContext: ExecutionContext): Future[Option[Int]] = {
    val q = (
      for {
        postExist <- posts.filter(r => r.id === vote.postId).exists.result if postExist
        voteExist <- postsVotes.filter(r => r.ip === vote.ip && r.postId === vote.postId).exists.result if !voteExist
        insert <- postsVotes += vote
        post <- queryById(vote.postId).result.head
        updatedPost = post.copy(votes = post.votes + vote.vote)
        updated <- queryById(vote.postId).update(updatedPost)
      } yield ()
      ).transactionally

    db.run(q)

    lookup(vote.postId) flatMap {
      case Some(r) => Future.successful(Some(r.votes))
      case None => Future.successful(None)
    }
  }

  class Posts(tag: Tag) extends Table[Post](tag, "posts") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def link = column[String]("link")
    def userId = column[Long]("userid")
    def created = column[Date]("created")
    def title = column[String]("title")
    def body = column[String]("body")
    def pictures = column[Int]("pictures")
    def votes = column[Int]("votes")
    def tags = column[List[String]]("tags")
    def visible = column[Boolean]("visible")
    def commentsNumber = column[Int]("commentsnumber")
    def user = foreignKey("posts_userid_fkey", userId, userDao.users)(_.id)

    def * = (id, link, userId, created, title, body, pictures, votes, tags, visible, commentsNumber) <> (Post.tupled, Post.unapply)
  }

  class PostVotes(tag: Tag) extends Table[PostVote](tag, "posts_votes") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("postId")
    def userId = column[Option[Long]]("userid")
    def vote = column[Int]("vote")
    def ip = column[String]("ip")

    def post = foreignKey("posts_votes_postid_fkey", postId, posts)(_.id)
    def user = foreignKey("posts_votes_userid_fkey", userId, userDao.users)(_.id)

    def * = (id, postId, userId, vote, ip) <> (PostVote.tupled, PostVote.unapply)
  }

}
