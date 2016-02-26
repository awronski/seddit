package services

import java.io.{File, FileInputStream, InputStream}

import actors.PicturesCacheActor.{GetUploadedPicturesList, UploadedPicture}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult}
import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.posts.{NotReadyPost, UserPicturesChacheKey}
import dao.PostDao
import models.User
import play.Logger
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class SavePostService @Inject()(@Named("picturesCacheActor") picturesCacheActor: ActorRef, postDao: PostDao) extends UserPicturesChacheKey {

  private implicit val timeout = Timeout(5 seconds)

  private val credentials: AWSCredentials = new BasicAWSCredentials(Conf.s3Key, Conf.s3Secret)
  private val client: AmazonS3Client = new AmazonS3Client(credentials)

  def save(post: NotReadyPost)(implicit user: User): Future[Long] = {

    val userPictures: Future[List[UploadedPicture]] =
      (picturesCacheActor ? GetUploadedPicturesList(picturesCacheKey(user))).mapTo[List[UploadedPicture]]

    val normalized = normalize(post)

    for {
      pictures <- userPictures
      post <- postDao.create(normalized)
      postId <- savePictures(post.id, user.id, pictures)
    } yield postId
  }

  private def normalize(post: NotReadyPost): NotReadyPost = {
    val newTags = post.tags
      .flatMap(_.split(","))
      .take(8)
      .map(_.trim())
      .map(t => if (t.length > 20) t.substring(0, 20) else t)

    post
      .copy(tags = newTags)
      .copy(body = post.body.trim, title = post.title.trim)
  }

  private def savePictures(postId: Long, userId: Long, pictures: List[UploadedPicture]) = Future[Long] {

    val reordered = pictures.last :: pictures.dropRight(1)
    val indexed: List[(UploadedPicture, Int)] = reordered.zipWithIndex

    indexed.par.foreach { case (uploaded, i) =>

      val file = new File(uploaded.filePath)
      val is: InputStream = new FileInputStream(file)

      val meta: ObjectMetadata = new ObjectMetadata
      meta.setContentLength(file.length())
      meta.setContentType(uploaded.contentType)
      meta.setCacheControl("max-age=604800")  //7 days

      val path = s"ali/${Conf.s3Folder}/pictures/$postId/${i}.jpg"
      val result: PutObjectResult = client.putObject(new PutObjectRequest(Conf.s3Bucket, path, is, meta))

      Logger.debug(s"File[$i] $file was added for post ${postId} with etag = ${result.getETag}")

    }

    postId
  }

}
