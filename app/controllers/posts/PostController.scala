package controllers.posts

import javax.inject.Inject

import akka.actor.ActorSystem
import controllers.UserAwareController
import dao._
import models.{CommentDetails, PostDetails}
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class PostController @Inject()(val messagesApi: MessagesApi)(postDao: PostDao, commentDao: CommentDao, implicit val userDao: UserDao, implicit val actorSystem: ActorSystem) extends UserAwareController with I18nSupport {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")

  def index(id: Long, title: String) = AsyncContextAwareAction { implicit request =>

    val post = postDao.details(id)
    val comments = post flatMap {
      case Some(r) => commentDao.commentsDetails(r._1.id)
      case _ => Future.successful(Seq.empty[CommentDetails])
    }

    val grouped = comments.map(s => s.groupBy(c => c.comment.topParentId))
    val listedMap = grouped.map(s => ListMap(s.toList:_*))
    val sorted = listedMap.map(s => s.mapValues(v => v.sortBy(_.comment.created)))

    val rc = for {
      r <- post
      c <- sorted
    } yield (r, c)

    rc map {
      case (Some(rd), s) => Ok(views.html.post(PostDetails(rd), s))
      case _ => NotFound(views.html.postNotFound())
    }

  }

}
