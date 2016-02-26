package controllers.utils

import java.text.Normalizer
import java.text.Normalizer.Form

object UrlNormalizer {

  def apply(url: String): String = {
    Normalizer
      .normalize(url.toLowerCase(), Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
      .replaceAll("[^\\p{Alnum}]+", "-")
      .replaceAll("-{2,}", "-")
      .replaceAll("^-", "")
      .replaceAll("-$", "")
  }

}
