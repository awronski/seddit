package actors

import javax.inject.Inject

import actors.PicturesCacheActor._
import akka.actor.{Actor, Props}
import play.Logger
import play.api.cache.{CacheApi, NamedCache}
import scala.concurrent.duration._
import scala.language.postfixOps

object PicturesCacheActor {

  def props = Props[PicturesCacheActor]

  case class CreateUploadedPicturesCache(key: String)
  case class GetUploadedPicturesList(key: String)
  case class GetUploadedPicturesNumber(key: String)
  case class UploadedPicture(file: String, path: String, ext: String, contentType: String) {
    def filePath: String = s"$path$file"
  }
  case class PutUploadedPicture(key: String, picture: UploadedPicture)

}

class PicturesCacheActor @Inject()(@NamedCache("picturesCache") picCache: CacheApi) extends Actor {

  def get(key: String): List[UploadedPicture] = {
    val pictures: List[UploadedPicture] = picCache.getOrElse[List[UploadedPicture]](key) { List[UploadedPicture]() }
    Logger.debug(s"Getting pictures with key $key, total ${pictures.size}")

    pictures
  }

  def put(u: PutUploadedPicture):Int = {
    val list = get(u.key) ::: List(u.picture)
    picCache.set(u.key, list, 30 minutes)
    val size = list.size
    Logger.debug(s"Putting picture with key ${u.key}, total $size")

    size
  }

  def create(k: String): Unit = {
    picCache.set(k, List[UploadedPicture](), 30 minutes)

    Logger.debug(s"Created pictures cache with key $k")
  }

  def receive = {
    case CreateUploadedPicturesCache(k) => create(k)
    case GetUploadedPicturesList(k) => sender ! get(k)
    case GetUploadedPicturesNumber(k) => sender ! get(k).size
    case u: PutUploadedPicture => sender ! put(u)
  }

  override def postStop() = {
    Logger.debug("Shutting down")
  }

}