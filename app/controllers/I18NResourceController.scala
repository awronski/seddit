package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import jsmessages.JsMessagesFactory
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class I18NResourceController @Inject()(jsMessagesFactory: JsMessagesFactory) extends Controller {

  val jsMessages = jsMessagesFactory.filtering(s =>
    s.startsWith("error.addForm.") || s.startsWith("dropzone.") || s.startsWith("error.comment"))

  val messages = Action { implicit request =>
    Ok(jsMessages(Some("window.Messages")))
  }

}
