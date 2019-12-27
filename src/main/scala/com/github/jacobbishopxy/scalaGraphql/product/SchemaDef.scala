package com.github.jacobbishopxy.scalaGraphql.product

import sangria.macros.derive._
import sangria.schema._

/**
 * Created by Jacob Xie on 12/25/2019
 */
object SchemaDef {

  import Repo._

  val IdentifiableType: InterfaceType[Unit, Identifiable] =
    InterfaceType(
      "Identifiable",
      "Entity that can be identified",
      fields[Unit, Identifiable](
        Field("id", StringType, resolve = _.value.id)
      )
    )


  implicit val PictureType: ObjectType[Unit, Picture] =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("The product picture"),
      DocumentField("url", "Picture CDN URL")
    )


  val ProductType: ObjectType[Unit, Product] =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture")
    )


  // define Query Type

  val Id: Argument[String] = Argument("id", StringType)

  val schema: ObjectType[Repo, Unit] =
    ObjectType(
      "Query",
      fields[Repo, Unit](
        Field(
          "product",
          OptionType(ProductType),
          description = Some("Returns a product with specific `id`."),
          arguments = Id :: Nil,
          resolve = c => c.ctx.product(c.arg(Id))
        ),
        Field(
          "products",
          ListType(ProductType),
          description = Some("Returns a list of all available products."),
          resolve = _.ctx.products
        )
      )
    )

}
