package controllers.xml

import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

import controllers.UserAwareController
import controllers.utils.{PostUrl, StringCut}
import dao.{SearchPostsParams, PostDao, UserDao}
import models.PostDetails
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, RequestHeader}
import services.Conf

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

//TODO check wheter use global ec or custom dispatcher

class RssController @Inject()(val messagesApi: MessagesApi, cached: Cached)(postDao: PostDao, implicit val userDao: UserDao) extends UserAwareController with I18nSupport {

  def rss = cached.status(_ => "rss", 200, 15 * 60) {
    Action.async { implicit request =>
      genRss(posts()).map(xml => Ok(xml))
    }
  }

  private def posts(): Future[Seq[PostDetails]] = {
    postDao.all(SearchPostsParams(0, 25))
  }

  private def genRss(postDetails: Future[Seq[PostDetails]])(implicit request: RequestHeader): Future[Elem] = {
    import xml.Utility.escape
    val format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)

    postDetails.map(rds => {
      <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
        <channel>
          <title>{Conf.siteName}</title>
          <description>Recenzje najciekawyszych produktów z AliExpress.com</description>
          <link>http://aliopinie.pl/rss</link>
          <atom:link href={Conf.siteUrl + "/rss.xml"} rel="self" type="application/rss+xml"/>
          {for (rd <- rds) yield {
          val link: String = PostUrl(rd.post.id, rd.post.title)
          <item>
            <title>{escape(rd.post.title)}</title>
            <description>{escape(StringCut(rd.post.body, 160))}</description>
            <link>{link}</link>
            <guid isPermaLink="true">{link}</guid>
            <pubDate>{format.format(rd.post.created)}</pubDate>
          </item>
        }}
        </channel>
      </rss>
    })
  }

}
