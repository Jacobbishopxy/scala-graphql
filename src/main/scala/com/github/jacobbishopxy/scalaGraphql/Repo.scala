package com.github.jacobbishopxy.scalaGraphql

import sangria.schema.{Field, ObjectType, Schema, fields}
import slick.jdbc.JdbcProfile

import prices.{SchemaDef => SchemaPrices}

/**
 * Created by Jacob Xie on 1/3/2020
 */
class Repo(val driver: JdbcProfile, dbCfg: String) {

  val resolverPrices = new prices.Resolver(driver, dbCfg)

  // init database, for demo
  new prices.Init(driver, dbCfg).initDatabase()


  private val queryFields: List[Field[Repo, Unit]] =
    SchemaPrices.FieldDef

  private val QueryType: ObjectType[Repo, Unit] =
    ObjectType("Query", fields[Repo, Unit](queryFields: _*))

  // schema definition
  val SD: Schema[Repo, Unit] = Schema(QueryType)

}


