package controllers.votes

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.util.Timeout
import controllers.{UserAwareController, WebContext}
import dao.UserDao
import models.{NewPostVote, PostVote}
import play.api.data.mapping._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import services.VotesService

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class VotesController @Inject()(votesService: VotesService, val messagesApi: MessagesApi)(implicit val userDao: UserDao, val actorSystem: ActorSystem) extends UserAwareController with I18nSupport {

  private implicit val timeout = Timeout(5 seconds)
  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")

  private def newPostVoteReads(implicit webContext: WebContext) = From[JsValue] { __ =>
    import play.api.data.mapping.json.Rules._

    ((__ \ "postId").read[Long] and
      (__ \ "vote").read(min(-1) |+| max(1))
      )(NewPostVote.apply _)
  }

  def vote = AsyncContextAwareAction { implicit webContext: WebContext =>

    val validated = newPostVoteReads.validate(webContext.request.body.asJson.get)

    val result = validated match {
      case s: Success[(Path, Any), NewPostVote] => success(s.get)
      case f: Failure[(Path, Any), NewPostVote] => Future.successful(Ok(Json.obj("error" -> "incorrect request")))
    }

    result
  }

  private def success(v: NewPostVote)(implicit webContext: WebContext) =  {
    val postVote = createPostVote(v, webContext)
    votesService.vote(postVote) flatMap {
      case Some(r) => Future.successful(Ok(Json.obj("result" -> (r + v.vote))))
      case None =>   Future.successful(Ok(Json.obj("error" -> "post not found")))
    }
  }

  def createPostVote(v: NewPostVote, webContext: WebContext): PostVote = {
    val userId = webContext.user.map(u => u.id)
    val ip = webContext.request.remoteAddress
    val vote = if (v.vote >= 0) 1 else -1
    PostVote(null.asInstanceOf[Long], v.postId, userId, vote, ip)
  }
}
