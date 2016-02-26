package controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import dao.UserDao
import jp.t2v.lab.play2.auth.AuthElement
import models.Role.NormalUser
import play.api.libs.json.{Json, JsObject}
import play.api.mvc._
import play.api.libs.json.Json.toJson

class TimeController @Inject()(implicit val userDao: UserDao) extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val user = loggedIn
    val json = Json.obj("user" -> user.email, "time" -> new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date()))
    Ok(json)
  }

}
