package controllers

import play.api.mvc.RequestHeader
import play.api.mvc.Results.Redirect
import play.api.{Application, GlobalSettings, Logger}
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader) = {
    val path = request.path.substring(0, Math.min(request.path.length, 128))
    Future(Redirect(controllers.routes.PageNotFoundController.index(), 301).flashing("path" -> path))
  }

  override def onStart(app: Application): Unit = {
    Logger.debug("Starting app")
    usdExchangeDeamon()
  }

  override def onStop(application: play.api.Application) {
    super.onStop(application)
    Logger.debug("Stopping app")
  }

  def usdExchangeDeamon(): Unit = {
    Akka.system.scheduler.schedule(10 seconds, 1 minute) {
//      Logger.debug("tick")
    }
  }

}
