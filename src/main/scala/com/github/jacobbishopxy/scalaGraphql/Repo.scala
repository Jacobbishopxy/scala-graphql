package com.github.jacobbishopxy.scalaGraphql

import sangria.schema.{Field, ObjectType, Schema, fields}
import slick.jdbc.H2Profile.api._


/**
 * Created by Jacob Xie on 1/3/2020
 */
class Repo(database: Database) {

  val resolverPrices = new prices.Resolver(database)
  val resolverPrices2 = new prices2.Resolver(database)

  // init database, for demo
  new prices.Init(database).initDatabase()
  new prices2.Init(database).initDatabase()

}

object Repo {

  import prices.SchemaDef.{FieldDef => FieldPrices}
  import prices2.SchemaDef.{FieldDef => FieldPrices2}


  val fld: List[Field[Repo, Unit]] =
    FieldPrices ::: FieldPrices2

  val QueryType: ObjectType[Repo, Unit] =
    ObjectType("Query", fields[Repo, Unit](fld: _*))

  val SD: Schema[Repo, Unit] = Schema(QueryType)


  def createDatabase(): Repo = {
    val db = Database.forConfig("h2mem")

    new Repo(db)
  }
}

