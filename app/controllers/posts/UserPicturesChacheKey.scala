package controllers.posts

import models.User

trait UserPicturesChacheKey {

  def picturesCacheKey(user: User): String = s"pictures/${user.id}"
  def picturesCacheKey(userId: Long): String = s"pictures/$userId"

}
