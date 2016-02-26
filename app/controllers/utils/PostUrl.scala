package controllers.utils

import play.api.mvc.RequestHeader

object PostUrl {

  def apply(id: Long, title: String)(implicit request: RequestHeader): String = {
    controllers.posts.routes.PostController.index(id, UrlNormalizer(title)).absoluteURL()
  }

}
