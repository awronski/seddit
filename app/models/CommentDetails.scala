package models

case class CommentDetails(comment: Comment, user: Option[User]) {

  def author: String = {
    user match {
      case Some(u) => u.nick
      case _ => comment.guest.get
    }
  }

  def isUser: Boolean = user.isDefined

}
