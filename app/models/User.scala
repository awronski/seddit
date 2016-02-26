package models

import java.util.Date

import controllers.register.AspiringUser
import models.Role.NormalUser
import org.mindrot.jbcrypt.BCrypt

case class User(id: Long, nick: String, email: String, password: String, created: Date, role: Role, sex: Sex) {
  def this(a: AspiringUser)
    = this(null.asInstanceOf[Long], a.nick, a.email, BCrypt.hashpw(a.password, BCrypt.gensalt), new java.util.Date(), NormalUser, Sex.valueOf(a.sex.charAt(0)))
}