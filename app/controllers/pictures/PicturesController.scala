package controllers.pictures

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.google.inject.name.Named
import controllers.UserLoggedController
import controllers.posts.UserPicturesChacheKey
import dao.UserDao
import jp.t2v.lab.play2.auth.LoginLogout
import models.Role.NormalUser
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MaxSizeExceeded, MultipartFormData, Result}
import services.{Conf, PicturesSavingService}

import scala.concurrent.duration._
import scala.language.postfixOps

class PicturesController @Inject()(@Named("picturesCacheActor") pictureCacheActor: ActorRef, val messagesApi: MessagesApi, picturesSavingService: PicturesSavingService)(implicit val userDao: UserDao, val actorSystem: ActorSystem) extends UserLoggedController with LoginLogout with I18nSupport with UserPicturesChacheKey {

  implicit val timeout = Timeout(Duration(5, TimeUnit.SECONDS))

    val jpg = """^image/jp(e)?g""".r
    val png = """^image/(x-)?png""".r

  def upload = StackAction(parse.maxLength(Conf.maxPictureSize * 1024 * 1024, parse.multipartFormData), AuthorityKey -> NormalUser) { implicit request =>
    request.body match {
      case Left(MaxSizeExceeded(length)) => BadRequest("Your file is too large, we accept just " + length + " bytes!")
      case Right(multipartForm) => processUpload(multipartForm, loggedIn)
    }
  }

  def processUpload(data: MultipartFormData[TemporaryFile], user: User): Result = {
    val result = {
      for {
        m <- data.file("pic") if m.contentType.isDefined && matchContentType(m.contentType.get)
        c <- m.contentType
        if c.contains("image/")
      } yield {
        saveFile(m, c, user)
      }
    } getOrElse {
      BadRequest("Error")
    }

    result
  }

  private def matchContentType(m: String): Boolean = {
    jpg.pattern.matcher(m).matches() || png.pattern.matcher(m).matches()
  }

  private def saveFile(f: FilePart[TemporaryFile], contentType: String, user: User): Result = {
    picturesSavingService.saveFile(f, contentType, user)
    Ok("Ok")
  }

}
