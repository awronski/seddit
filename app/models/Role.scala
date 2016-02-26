package models

sealed trait Role

object Role {

  case object Admin extends Role

  case object NormalUser extends Role

  def valueOf(value: String): Role = value match {
    case "Admin" => Admin
    case "NormalUser" => NormalUser
    case _ => throw new IllegalArgumentException()
  }

  def stringOf(value: Role): String = value match {
    case Admin => "Admin"
    case NormalUser => "NormalUser"
    case _ => throw new IllegalArgumentException()
  }

}
