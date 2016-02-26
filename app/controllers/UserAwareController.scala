package controllers

import jp.t2v.lab.play2.auth.{AuthElement, OptionalAuthElement}
import models.Role.NormalUser
import play.api.mvc.{Action, AnyContent, Controller, Result}

import scala.concurrent.Future

abstract class UserAwareController extends Controller with OptionalAuthElement with AuthConfigImpl {

  def AsyncContextAwareAction(f: WebContext => Future[Result]): Action[AnyContent] = {
    AsyncStack { implicit request =>
      f(WebContext(loggedIn, request))
    }
  }

  def ContextAwareAction(f: WebContext => Result): Action[AnyContent] = {
    StackAction() { implicit request =>
      f(WebContext(loggedIn, request))
    }
  }

}
