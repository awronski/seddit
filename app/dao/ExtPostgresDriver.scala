package dao

import com.github.tminglei.slickpg._

trait ExtPostgresDriver extends ExPostgresDriver with PgArraySupport {

  override val api = extApi
  object extApi extends API with ArrayImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }
  
}

object ExtPostgresDriver extends ExtPostgresDriver