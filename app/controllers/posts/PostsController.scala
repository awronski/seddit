package controllers.posts

import javax.inject.Inject

import akka.actor.ActorSystem
import controllers.{UserAwareController, WebContext}
import dao._
import models.PostDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result

import scala.concurrent.Future

class PostsController @Inject()(val messagesApi: MessagesApi)(postDao: PostDao, implicit val userDao: UserDao, implicit val actorSystem: ActorSystem) extends UserAwareController with I18nSupport {

  private implicit val ec = actorSystem.dispatchers.lookup("ali.dispatcher")
  private val perPage: Int = play.api.Play.current.configuration.getInt("ali.posts.per-page").getOrElse(10)

  def defaultParams(page: Int): SearchPostsParams =  SearchPostsParams(page * perPage, perPage)
  def paramsWithNick(page: Int, nick: String) = defaultParams(page).copy(nick = Some(nick))
  def paramsWithTag(page: Int, tag: String) = defaultParams(page).copy(tag = Some(tag))

  def index() = page(0)

  def page(page: Int) = AsyncContextAwareAction { implicit request =>
    val postDetails = postDao.all(defaultParams(page))
    val counter = postDao.count(SearchPostsParams(true))

    results(postDetails, counter, PostsParams(PostType.NORMAL, page, 0, perPage))
  }

  def pageByNick(nick: String, page: Int) = AsyncContextAwareAction { implicit request =>
    val postDetails = postDao.all(paramsWithNick(page, nick))
    val counter = postDao.count(SearchPostsParams(true).copy(nick = Some(nick)))

    results(postDetails, counter, PostsParams(PostType.BY_NICK, page, 0, perPage, Some(nick)))
  }

  def pageByTag(tag: String, page: Int) = AsyncContextAwareAction { implicit request =>
    val postDetails = postDao.all(paramsWithTag(page, tag))
    val counter = postDao.count(SearchPostsParams(true).copy(tag = Some(tag)))

    results(postDetails, counter, PostsParams(PostType.BY_TAG, page, 0, perPage, Some(tag)))
  }

  private def results(postDetails: Future[Seq[PostDetails]], counter: Future[Int], params: PostsParams)(implicit webContext: WebContext): Future[Result] = {
    for {
      total <- counter
      posts <- postDetails
    } yield Ok(views.html.posts(posts, params.copy(pages = Math.ceil(total.toDouble / perPage).toInt)))

  }

}
