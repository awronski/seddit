package controllers.comments

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.util.Timeout
import controllers.posts.UserPicturesChacheKey
import controllers.utils.StringCut
import controllers.{UserAwareController, WebContext}
import dao.{CommentDao, PostDao, UserDao}
import jp.t2v.lab.play2.auth.LoginLogout
import models.{CommentDetails, PostedComment, Post}
import play.api.data.mapping._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class CommentsController @Inject()(val messagesApi: MessagesApi, postDao: PostDao, commentDao: CommentDao)(implicit val userDao: UserDao, val actorSystem: ActorSystem) extends UserAwareController with LoginLogout with I18nSupport with UserPicturesChacheKey {

  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

  private def potedCommentReads(implicit webContext: WebContext) = From[JsValue] { __ =>
    import play.api.data.mapping.json.Rules._

    ((__ \ "postId").read(transformAndValidPostId) and
      (__ \ "nick").read(transformAndValidNick |+| minLength(1) |+| maxLength(64)) and
      (__ \ "body").read(transformAndValidBody)
      )(PostedComment.apply _)
  }

  private def transformAndValidPostId = Rule.fromMapping[Long, Long] {
    v => {
      val post: Option[Post] = Await.result(postDao.lookup(v), 5 seconds)
      post match {
        case Some(r) => Success(r.id)
        case None => Failure(Seq(ValidationError("error.addCommentForm.post.not.found")))
      }
    }
  }

  private def transformAndValidNick(implicit webContext: WebContext) = Rule.fromMapping[String, String] {
    v => {
      val user = webContext.user
      user match {
        case Some(u) => Success(u.nick)
        case None =>
          if (v.matches("^([a-zA-Z0-9\\-\\.]+)$"))
            Success(v.trim)
          else
            Failure(Seq(ValidationError("error.nick.illegal.characters")))
      }
    }
  }

  private def transformAndValidBody = Rule.fromMapping[String, String] {
    v => {
      if (v.trim.length > 0)
        Success(StringCut(v.trim, 1024))
      else {
        Failure(Seq(ValidationError("error.comment.body.is.required")))
      }
    }
  }

  def saveComment = AsyncContextAwareAction { implicit webContext: WebContext =>
    val validated = potedCommentReads.validate(webContext.request.body.asJson.get)
    implicit val user = webContext.user

    val result = validated match {
      case s: Success[(Path, Any), PostedComment] => success(s.get)
      case f: Failure[(Path, Any), PostedComment] => error(f.errors)
    }

    result
  }

  private def success(posted: PostedComment)(implicit user: Option[User], webContext: WebContext): Future[play.api.mvc.Result] = {
    for {
      cd <- commentDao.create(posted, webContext.request.remoteAddress)
      result <- Future.successful(Ok(jsonSucessResult(cd)))
    } yield result
  }

  def jsonSucessResult(cd: CommentDetails): String = {
    import com.codahale.jerkson.Json._
    generate(
      Map(
        "commentId" -> cd.comment.id,
        "created" -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(cd.comment.created),
        "author" -> cd.author,
        "body" -> cd.comment.body.replace("\n", "<br/>"),
        "logged" -> cd.comment.userId.isDefined
      )
    )
  }

  private def error(errors: Seq[(Path, Any)])(implicit webContext: WebContext): Future[play.api.mvc.Result] = {
    import com.codahale.jerkson.Json._
    Future.successful(
      Ok(
        generate(
          Map(
            "errors" -> errors.map({ case (k, v) => k.toString().substring(1) })
          )
        )
      )
    )
  }

}
