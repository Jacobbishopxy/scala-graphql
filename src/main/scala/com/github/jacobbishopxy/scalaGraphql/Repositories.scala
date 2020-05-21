package com.github.jacobbishopxy.scalaGraphql

import slick.jdbc.JdbcProfile


/**
 * Created by Jacob Xie on 1/3/2020
 */
class Repositories(val driver: JdbcProfile, dbCfg: String) {

  val resolverPrices = new prices.Resolver(driver, dbCfg)

  // init database, for demo
  new prices.Init(driver, dbCfg).initDatabase()
}

