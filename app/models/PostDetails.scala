package models

case class PostDetails(post: Post, user: User)

object PostDetails {
  def apply(r: (Post, User)) = new PostDetails(r._1, r._2)
}



