package com.github.jacobbishopxy.scalaGraphql

import sangria.schema.{Field, ObjectType, Schema, fields}

import prices.{SchemaDef => SchemaPrices}

/**
 * Created by Jacob Xie on 5/21/2020
 */
object Schemas {
  private val queryFields: List[Field[Repositories, Unit]] =
    SchemaPrices.FieldDef

  private val queryType: ObjectType[Repositories, Unit] =
    ObjectType("Query", fields[Repositories, Unit](queryFields: _*))

  // schema definition
  val SD: Schema[Repositories, Unit] = Schema(queryType)
}
