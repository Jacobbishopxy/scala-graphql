package com.github.jacobbishopxy.scalaGraphql

import sangria.schema.{Field, ObjectType, Schema, fields}
import slick.jdbc.H2Profile.api._


/**
 * Created by Jacob Xie on 1/3/2020
 */
class Repo(database: Database) {

  val resolverPrices = new prices.Resolver(database)

  // init database, for demo
  new prices.Init(database).initDatabase()

}

object Repo {

  import prices.SchemaDef.{FieldDef => FieldPrices}


  val fld: List[Field[Repo, Unit]] =
    FieldPrices

  val QueryType: ObjectType[Repo, Unit] =
    ObjectType("Query", fields[Repo, Unit](fld: _*))

  val SD: Schema[Repo, Unit] = Schema(QueryType)


  def createDatabase(): Repo = {
    val db = Database.forConfig("h2mem")

    new Repo(db)
  }
}

