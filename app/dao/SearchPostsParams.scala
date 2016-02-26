package dao

case class SearchPostsParams(offset: Option[Int], limit: Option[Int], nick: Option[String], tag: Option[String], visible: Option[Boolean] = Some(true))

object SearchPostsParams {
  def apply(offset: Int, limit: Int) = new SearchPostsParams(Some(offset), Some(limit), None, None, Some(true))
  def apply(visible: Boolean) = new SearchPostsParams(None, None, None, None, Some(visible))
}