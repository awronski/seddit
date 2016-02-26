package models

case class PostVote(id: Long, postId: Long, userId: Option[Long], vote: Int, ip: String)
