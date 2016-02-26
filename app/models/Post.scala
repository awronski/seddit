package models

import java.util.Date

import controllers.posts.NotReadyPost

case class Post(id: Long, link: String, userId: Long, created: Date, title: String, body: String, pictures: Int, votes: Int, tags: List[String], visible: Boolean, commentsNumber: Int) {
  def this(r: NotReadyPost, userId: Long)
    = this(null.asInstanceOf[Long], r.link, userId, new Date(), r.title, r.body, r.pictures, 1, r.tags, true, 0)
}
