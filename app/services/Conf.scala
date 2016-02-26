package services

import play.api.Play.current

object Conf {

  val tempDirectory = current.configuration.getString("pictures.temp.directory").get

  val maxPictureSize = current.configuration.getInt("ali.pictures.maxSize").getOrElse(10)

  val s3Bucket = current.configuration.getString("s3.bucket").get
  val s3Key = current.configuration.getString("s3.key").get
  val s3Secret = current.configuration.getString("s3.secret").get
  val s3Folder = current.configuration.getString("s3.folder").get

  val siteUrl = current.configuration.getString("site.url").getOrElse("http://?siteurl?")
  val siteName = current.configuration.getString("site.name").getOrElse("?sitename?")

}
