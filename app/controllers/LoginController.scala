package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.data.Form
import dao._
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits.defaultContext


import scala.concurrent.Future

class LoginController @Inject()(val messagesApi: MessagesApi)(implicit val userDao: UserDao, implicit val actorSystem: ActorSystem) extends UserAwareController with LoginLogout with I18nSupport {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")

  def remembermeForm(implicit lang: play.api.i18n.Lang) = Form {
    "rememberme" -> boolean
  }

  def loginForm(implicit lang: play.api.i18n.Lang) = Form {
    mapping("email" -> email, "password" -> text)(userDao.authenticate)(_.map(u => (u.email, "")))
      .verifying(Messages("error.invalid.email.or.password"), result => result.isDefined)
  }

  def login = ContextAwareAction { implicit request =>
    Ok(views.html.login(loginForm, remembermeForm.fill(request.session.get("rememberme").exists("true" ==))))
  }

  def logout = AsyncContextAwareAction{ implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "success" -> "You've been logged out"
    ))
  }

  def authenticate = AsyncContextAwareAction { implicit request =>
    val rememberme = remembermeForm.bindFromRequest()
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors, rememberme))),
      { user =>
        val req = request.copy(tags = request.tags + ("rememberme" -> rememberme.get.toString))
        gotoLoginSucceeded(user.get.id)(req, defaultContext).map(_.withSession("rememberme" -> rememberme.get.toString))
      }
    )
  }

}
