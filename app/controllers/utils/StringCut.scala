package controllers.utils

object StringCut {

  def apply(s: String, max: Int): String = {
    if (s.length > max) s.substring(0, max - 1) else s
  }

}
