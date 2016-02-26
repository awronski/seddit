package controllers

import play.Logger
import dao.UserDao
import jp.t2v.lab.play2.auth.AuthConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import models.Role._
import scala.concurrent.{Future, ExecutionContext}
import scala.reflect._
import play.api.mvc.Result

trait AuthConfigImpl extends AuthConfig {

  type Id = Long
  type User = models.User
  type Authority = models.Role

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600
  implicit val userDao: UserDao

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = {
    userDao.lookup(id)
  }

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    val uri = request.session.get("access_uri").getOrElse(posts.routes.PostsController.index().url.toString)
    Future.successful(Redirect(uri).withSession(request.session - "access_uri"))
  }

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(posts.routes.PostsController.index()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.LoginController.login()).withSession("access_uri" -> request.uri))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Logger.info(s"authorizationFailed. userId: ${user.id}, userName: ${user.email}, authority: $authority")
    Future.successful(Forbidden("no permission"))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user.role, authority) match {
      case (Admin, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }
  }

  override lazy val tokenAccessor = new RememberMeTokenAccessor(sessionTimeoutInSeconds)

}
