package com.github.jacobbishopxy.scalaGraphql.dynamic

import slick.jdbc.JdbcProfile

/**
 * Created by Jacob Xie on 5/26/2020
 */
trait DatabaseComponent {
  val driver: JdbcProfile
  val dbCfg: String

  val db: driver.backend.DatabaseDef = driver.backend.Database.forConfig(dbCfg)
}

