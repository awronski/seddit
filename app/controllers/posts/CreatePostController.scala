package controllers.posts

import javax.inject.Inject

import actors.PicturesCacheActor.{CreateUploadedPicturesCache, GetUploadedPicturesNumber}
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import controllers.utils.UrlNormalizer
import controllers.{UserLoggedController, WebContext}
import dao._
import play.Logger
import play.api.cache.CacheApi
import play.api.data.mapping._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.{PicturesSavingService, SavePostService}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class CreatePostController @Inject()(
                                      @Named("picturesCacheActor") pictureCacheActor: ActorRef,
                                      ws: WSClient,
                                      val messagesApi: MessagesApi,
                                      savePostService: SavePostService,
                                      picturesSavingService: PicturesSavingService,
                                      cache: CacheApi)
                                    (implicit val userDao: UserDao, implicit val actorSystem: ActorSystem) extends UserLoggedController with I18nSupport with UserPicturesChacheKey {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")
  implicit val timeout = Timeout(5 seconds)

  def addPost() = ContextAwareForLoggedUserAction { implicit webContext: WebContext =>
    (pictureCacheActor ? CreateUploadedPicturesCache(picturesCacheKey(webContext.user.get))).mapTo
    Ok(views.html.createPost())
  }

  private def notReadyPostReads(implicit webContext: WebContext) = From[JsValue] { __ =>
        import play.api.data.mapping.json.Rules._

        ((__ \ "title").read(minLength(8) |+| maxLength(160)) and
         (__ \ "link").read(transformAndValidLink) and
         (__ \ "body").read(minLength(16) |+| maxLength(10 * 1024)) and
         (__ \ "pictures").read(transformAndValidPicture) and
         (__ \ "tags").read(transformAndValidTags)
         )(NotReadyPost.apply _)
  }

  private def transformAndValidLink = Rule.fromMapping[String, String] {
    v => {
      if (checkUrlSyntax(v))
        Success(v)
      else
        Failure(Seq(ValidationError("error.addForm.link")))
    }
  }

  private def transformAndValidPicture(implicit webContext: WebContext) = Rule.fromMapping[Int, Int] {
    v => {
      val user = webContext.user.get
      val future = (pictureCacheActor ? GetUploadedPicturesNumber(picturesCacheKey(user))).mapTo[Int]
      val pictures = Await.result(future, 5 seconds)

      pictures match {
        case 0 => Failure(Seq(ValidationError("error.addForm.add.at.least.one.picture.must.be.added")))
        case _ => Success(pictures)
      }
    }
  }

  private def transformAndValidTags(implicit webContext: WebContext) = Rule.fromMapping[List[String], List[String]] {
    v => {
      if (v.nonEmpty)
        Success(v)
      else
        Failure(Seq(ValidationError("error.addForm.add.at.least.one.tag")))
    }
  }

  private def checkUrlSyntax(link: String): Boolean = {
    val url = """^(http(s)?://)(.*)"""

    link.matches(url) match {
      case true => true
      case _ =>
        Logger.warn(s"Link doesn't match ${link}")
        false
    }
  }

  private def error(errors: Seq[(Path, Any)])(implicit webContext: WebContext): Future[play.api.mvc.Result] = {
    Future.successful(
      Ok(
        Json.obj(
          "results" -> toJson(
            Map(
              "errors" -> toJson(
                errors.map({ case (k, v) => k.toString().substring(1) }))
            )
          )
        )
      )
    )
  }

  def savePost = AsyncContextAwareForLoggedUserAction { implicit webContext: WebContext =>
    implicit val user: User = loggedIn(webContext.request)

    val validated = notReadyPostReads.validate(webContext.request.body.asJson.get)
    val result = validated match {
      case s: Success[(Path, Any), NotReadyPost] => success(s.get)
      case f: Failure[(Path, Any), NotReadyPost] => error(f.errors)
    }

    result
  }

  private def success(post: NotReadyPost)(implicit user: User, webContext: WebContext): Future[play.api.mvc.Result] = {
    for {
      postId <- savePostService.save(post)
      result <- Future.successful(Ok(jsonSucessResult(post, postId)))
    } yield result

  }

  def jsonSucessResult(post: NotReadyPost, postId: Long): String = {
    import com.codahale.jerkson.Json._
    generate(
      Map(
        "results" ->
          Map(
            "id" -> postId, "title" -> UrlNormalizer(post.title)
          )
      )
    )
  }

}
