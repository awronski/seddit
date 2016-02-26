package controllers.posts

import controllers.posts.PostType.{PostType, _}
import play.api.mvc.Call

case class PostsParams(postType: PostType, page: Int, pages: Int, perPage: Int, param: Option[String] = None) {

  def url(page: Int): Call = {
    postType match {
      case BY_NICK if param.isDefined => controllers.posts.routes.PostsController.pageByNick(param.get, page)
      case BY_TAG if param.isDefined => controllers.posts.routes.PostsController.pageByTag(param.get, page)
      case _ => controllers.posts.routes.PostsController.page(page)
    }
  }

  def urlChange(change: Int): Call = {
    val newPage = page + change
    url(newPage)
  }

}
