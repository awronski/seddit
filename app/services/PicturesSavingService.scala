package services

import java.io.{FileOutputStream, File}
import javax.imageio.ImageIO
import javax.inject.Inject

import actors.PicturesCacheActor
import actors.PicturesCacheActor.{PutUploadedPicture, UploadedPicture}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.name.Named
import controllers.posts.UserPicturesChacheKey
import models.User
import org.imgscalr.Scalr
import play.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Random

class PicturesSavingService @Inject()(@Named("picturesCacheActor") pictureCacheActor: ActorRef) extends UserPicturesChacheKey {

  implicit val timeout = Timeout(5 seconds)

  def saveFile(f: FilePart[TemporaryFile], contentType: String, user: models.User): Unit = {
    val uploaded = uploadedPicture(contentType)

    val imgFile: File = new File(s"${Conf.tempDirectory}${uploaded.file}")
    f.ref.moveTo(imgFile)

    resizeAndResave(uploaded, user)
  }

  def saveFile(picture: Array[Byte], contentType: String, user: models.User): Unit = {
    val uploaded = uploadedPicture(contentType)

    val stream: FileOutputStream = new FileOutputStream(uploaded.filePath)
    try {
      stream.write(picture)
    } finally {
      stream.close()
    }

    resizeAndResave(uploaded, user)
  }

  def resizeAndResave(uploaded: UploadedPicture, user: User): Unit = {
    val imgFile: File = new File(uploaded.filePath)
    val img = ImageIO.read(imgFile)
    val resized = Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, 800, 800, Scalr.OP_ANTIALIAS)
    ImageIO.write(resized, "jpg", imgFile)

    (pictureCacheActor ? PutUploadedPicture(picturesCacheKey(user), uploaded)).mapTo[Int].map(i =>
      Logger.debug(s"saved picture ${uploaded.filePath} for user ${user.id}, total = $i")
    )
  }

  def uploadedPicture(contentType: String): PicturesCacheActor.UploadedPicture = {
    val ext = "jpg"
    val file = s"${System.currentTimeMillis()}_${new Random().nextInt(1000)}.$ext"
    UploadedPicture(file, Conf.tempDirectory, ext, contentType)
  }
}
