package controllers.register

import javax.inject.Inject

import akka.actor.ActorSystem
import com.nappin.play.recaptcha.RecaptchaVerifier
import controllers.UserAwareController
import dao.UserDao
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class RegisterController @Inject()(val messagesApi: MessagesApi, val verifier: RecaptchaVerifier)(implicit val userDao: UserDao, val actorSystem: ActorSystem) extends UserAwareController with LoginLogout with I18nSupport {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")

  def userForm(implicit lang: play.api.i18n.Lang) = Form(
    play.api.data.Forms.mapping (
      "nick" -> nonEmptyText
        .verifying("error.nick.already.taken", validateNick _)
        .verifying("error.nick.illegal.characters", _.matches("^([a-zA-Z0-9\\-\\.]+)$"))
        .verifying("error.nick.max.length", _.length <= 64),
      "email" -> email.verifying("error.account.with.that.email.already.exists", validateEmail _),
      "password" -> tuple(
        "password1" -> text.verifying("error.password.length", p => p.length >= 6 && p.length <= 64),
        "password2" -> text
      ).verifying("error.password.dont.match", password => password._1 == password._2)
        .transform(
        { case (password1, password2) => password1 },
        (password1: String) => ("", "")
      ),
      "sex" -> text.verifying("error.account.enter.your.gender", sex => sex == "f" || sex == "m")
    )
    (AspiringUser.apply)(AspiringUser.unapply)
  )

  def validateEmail(email: String): Boolean = {
    Await.result(userDao.lookup(email), 5 seconds).isEmpty
  }

  def validateNick(nick: String): Boolean = {
    Await.result(userDao.lookupByNick(nick), 5 seconds).isEmpty
  }

  def register = ContextAwareAction { implicit request =>
    Ok(views.html.register(userForm))
  }

  def createUser = AsyncContextAwareAction { implicit request =>
    verifier.bindFromRequestAndVerify(userForm).flatMap { form =>
      form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.register(formWithErrors))),
        a => userDao.create(a).flatMap {
          case Some(u) => gotoLoginSucceeded(u.id)
          case None => Future.successful(InternalServerError(views.html.register(userForm)))
        }
      )
    }
  }

}
