package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import dao._
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.Future

class PageNotFoundController @Inject()(val messagesApi: MessagesApi)(implicit val userDao: UserDao, implicit val actorSystem: ActorSystem) extends UserAwareController with I18nSupport {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")

  def index = AsyncContextAwareAction { implicit request =>
    val path = request.flash.get("path").getOrElse("-")
    Future(NotFound(views.html.pageNotFound(path)))
  }

}
