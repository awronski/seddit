package controllers.xml

import javax.inject.Inject

import controllers.UserAwareController
import controllers.utils.PostUrl
import dao.{SearchPostsParams, PostDao, UserDao}
import models.PostDetails
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, RequestHeader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

//TODO check wheter use global ec or custom dispatcher

class SitemapController @Inject()(val messagesApi: MessagesApi, cached: Cached)(postDao: PostDao, implicit val userDao: UserDao) extends UserAwareController with I18nSupport {

  def sitemap = cached.status(_ => "sitemap", 200, 15 * 60) {
    Action.async { implicit request =>
      genRss(posts()).map(xml => Ok(xml))
    }
  }

  private def posts(): Future[Seq[PostDetails]] = {
    postDao.all(SearchPostsParams(true))
  }

  private def genRss(postDetails: Future[Seq[PostDetails]])(implicit request: RequestHeader): Future[Elem] = {
    postDetails.map(rds => {
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
          {for (rd <- rds) yield {
            <url><loc>{PostUrl(rd.post.id, rd.post.title)}</loc></url>
        }}
        </urlset>
    })
  }

}
