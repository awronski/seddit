package controllers

import jp.t2v.lab.play2.stackc.RequestWithAttributes
import models.User
import play.api.mvc.{WrappedRequest, AnyContent}

case class WebContext(user: Option[User], request: RequestWithAttributes[AnyContent]) extends WrappedRequest(request)
