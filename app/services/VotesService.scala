package services

import javax.inject.Inject

import dao.PostDao
import models.PostVote
import play.api.cache._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class VotesService @Inject()(@NamedCache("votesCache") votesCache: CacheApi, postDao: PostDao) {


  def vote(vote: PostVote): Future[Option[Int]] = {

    votesCache.get[Option[Int]](vote) match {
      case Some(cached) => Future.successful(cached)
      case None => fromDb(vote)
    }
  }

  def fromDb(vote: PostVote) = {
    val voted: Future[Option[Int]] = postDao.createVoteIfNotVoted(vote)
    voted.foreach(f => votesCache.set(vote, f, 1 hour))
    voted
  }

  implicit private def cacheKey(vote: PostVote): String = {
    s"${vote.postId}/${vote.ip}"
  }

}
