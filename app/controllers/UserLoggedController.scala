package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.Role.NormalUser
import play.api.mvc._
import play.api.mvc.AnyContent

import scala.concurrent.Future

abstract class UserLoggedController extends Controller with AuthConfigImpl with AuthElement {

  def AsyncContextAwareForLoggedUserAction(f: WebContext => Future[Result]): Action[AnyContent] = {
    AsyncStack(AuthorityKey -> NormalUser) { implicit request =>
      f(WebContext(Some(loggedIn), request))
    }
  }

  def ContextAwareForLoggedUserAction(f: WebContext => Result): Action[AnyContent] = {
    StackAction(AuthorityKey -> NormalUser) { implicit request =>
      f(WebContext(Some(loggedIn), request))
    }
  }

}
