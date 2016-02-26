package models

import java.util.Date

case class Comment(id: Long, parentId: Option[Long], topParentId: Option[Long], postId: Long, userId: Option[Long], guest: Option[String], body: String, created: Date, ip: String) {

  def this(c: PostedComment, user: Option[User], ip: String) {
    this(
      null.asInstanceOf[Long],
      None,
      None,
      c.postId,
      user.map(u => u.id).orElse(None),
      if (user.isDefined) None else Some(c.nick),
      c.body,
      new Date(),
      ip)
  }

}
